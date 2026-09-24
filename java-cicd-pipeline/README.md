# Java CI/CD Pipeline Demo

End-to-end CI/CD pipeline for a Java (Spring Boot) app, matching this toolchain:

```
Git/GitHub + Java  -->  Jenkins  <-->  Ansible  <-->  Kubernetes
                           ^              ^
                           |              |
                        Maven          Docker
                           ^
                           |
                       Terraform ---> AWS (EKS, VPC, ECR)
```

## What's in this repo

| Path | Purpose |
|---|---|
| `src/` | Java 17 / Spring Boot REST app (build + unit tests) |
| `pom.xml` | Maven build config (JaCoCo coverage, Surefire tests) |
| `Jenkinsfile` | Declarative pipeline: checkout → build/test → Docker build/push → Terraform → Ansible → deploy to K8s |
| `Dockerfile` | Multi-stage build producing a small runtime image |
| `terraform/` | Provisions AWS VPC, EKS cluster, node group, IAM roles, ECR repo |
| `ansible/` | Configures Docker on build hosts and kubectl/AWS CLI on Jenkins agents |
| `k8s/` | Namespace, Deployment (3 replicas, health probes), LoadBalancer Service |

## Pipeline flow

1. **Git/GitHub** — developer pushes code; Jenkins is triggered by a webhook.
2. **Jenkins + Maven + Java** — checks out the repo, runs `mvn clean verify` (unit tests + JaCoCo coverage).
3. **Docker** — builds a multi-stage image and pushes it to Docker Hub/ECR.
4. **Terraform + AWS** — provisions/updates the EKS cluster, VPC, and supporting infra (idempotent `plan` → `apply`).
5. **Ansible** — configures build/agent hosts (Docker engine, kubectl, AWS CLI) so they're ready to deploy.
6. **Kubernetes** — Jenkins updates kubeconfig for the EKS cluster and applies the namespace, Deployment, and Service manifests, then waits on rollout status.

## Local build & run (before wiring up Jenkins)

```bash
mvn clean verify              # build + test
mvn spring-boot:run           # run locally on :8080
curl http://localhost:8080/api/hello?name=Jenkins

docker build -t devops-demo-app:local .
docker run -p 8080:8080 devops-demo-app:local
```

## Jenkins setup checklist

1. Install plugins: Pipeline, Git, Docker Pipeline, Kubernetes CLI, Terraform, Ansible, JaCoCo.
2. Add credentials in Jenkins (Manage Jenkins → Credentials):
   - `dockerhub-creds` (username/password)
   - `aws-creds` (AWS access key/secret, or use an instance profile instead)
3. Configure tools: JDK 17 as `JDK-17`, Maven as `Maven-3.9` (Manage Jenkins → Tools).
4. Create a new **Pipeline** job → "Pipeline script from SCM" → point at this repo's `Jenkinsfile`.
5. Add a GitHub webhook (`Settings → Webhooks`) pointing at `http://<jenkins-url>/github-webhook/` so pushes trigger builds automatically.

## Terraform setup

```bash
cd terraform
terraform init      # uses the S3 backend defined in provider.tf — update bucket name first
terraform plan
terraform apply
```

## Ansible setup

Update `ansible/inventory.ini` with your real host IPs/key, then:

```bash
cd ansible
ansible-playbook -i inventory.ini playbook.yml
```

## Kubernetes

```bash
aws eks update-kubeconfig --name devops-demo-cluster --region ap-south-1
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get svc -n devops-demo    # get the LoadBalancer external IP
```

## Pushing this repo to GitHub

```bash
cd java-cicd-pipeline
git init
git add .
git commit -m "Initial commit: full CI/CD pipeline (Jenkins, Maven, Docker, Terraform, Ansible, K8s)"
git branch -M main
git remote add origin https://github.com/<your-username>/java-cicd-pipeline.git
git push -u origin main
```

Then update the `git url` in the `Jenkinsfile`'s Checkout stage to point at your new GitHub repo, and the `DOCKERHUB_REPO` variable to your own Docker Hub (or ECR) namespace.

## Notes / things to customize before production use

- Replace `yourdockerhubuser` and `<your-username>` placeholders throughout.
- The Terraform S3 backend bucket must exist before `terraform init` (or switch to local state for a quick test).
- Node/agent IPs in `ansible/inventory.ini` are placeholders — replace with your real EC2 instances.
- Consider adding a `Trivy`/`SonarQube` stage to the Jenkinsfile for security/code-quality scanning.
