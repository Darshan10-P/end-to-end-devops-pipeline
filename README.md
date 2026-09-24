# end-to-end-devops-pipeline
End-to-end DevOps CI/CD pipeline using Jenkins, Docker, Ansible, Terraform, Kubernetes, and AWS

<img width="1774" height="887" alt="JavaCICD" src="https://github.com/user-attachments/assets/e7bc9ae0-9515-4040-996f-2644d48cfbf2" />

# Tech stack
Tool	  |  Role in the pipeline
|---|---|
| Git / GitHub | Source control, triggers Jenkins via webhook |
| Java + Maven | App build, unit tests, packaging |
| Jenkins | Orchestrates every CI/CD stage |
| Docker | Packages the app into a container image |
| Terraform | Provisions AWS infra (VPC, EKS, ECR, IAM) |
| Ansible | Configures build/agent hosts |
| Kubernetes (EKS) | Runs and scales the deployed app |
| AWS | Cloud provider hosting everything |

#Prerequisites
- An AWS account with an IAM user that has programmatic access (Access Key ID + Secret Access Key)
- An EC2 key pair (for SSH access to instances)
- A Linux EC2 instance (Amazon Linux 2 or 2023) to act as the Jenkins/build server
- Git installed locally
- A GitHub repository to push this project to

 # Repo structure
- `README.md`
- `pom.xml`
- `src/`
  - `main/java/` — application code
  - `test/java/` — unit tests
- `Jenkinsfile`
- `Dockerfile`
- `terraform/` — AWS infra as code
- `ansible/` — host configuration
- `k8s/` — Kubernetes manifests

# Steps to install Java and Maven

## Step 1: Update packages
sudo yum update -y
##Step 2: Install Java (OpenJDK 17)
sudo yum install -y java-17-amazon-corretto
##Step 3: Verify Java installation
java -version
##Step 4: Install Maven
sudo yum install -y maven
##Step 5: Verify Maven installation
mvn -version

#Steps to install Docker on Linux instance
##Step 1: Update packages
sudo yum update -y
#Step 2: Install Docker
sudo yum install -y docker
#Step 3: Start and enable Docker service
sudo systemctl start docker
sudo systemctl enable docker
#Step 4: Add current user to docker group
sudo usermod -aG docker $USER
newgrp docker
Step 5: Verify installation
docker --version

#Steps to install Terraform on Linux instance
## Step 1: Update packages
sudo yum update -y
## Step 2 Install required tools
sudo yum install -y yum-utils
## Step 3: Add HashiCorp repository
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
## Step 4:Install Terraform
sudo yum install -y terraform
## Step 5:Verify installation
terraform version

#Steps to install Ansible on Linux instance
##Step 1: Update packages
sudo yum update -y
##Step 2: Install EPEL / Python pip (if required)
sudo yum install -y python3-pip
##Step 3: Install Ansible
sudo pip3 install ansible
##Step 4: Verify installation
ansible --version

#Steps to install kubectl on Linux instance
##Step 1: Download kubectl binary
curl -LO "https://dl.k8s.io/release/v1.30.0/bin/linux/amd64/kubectl"
##Step 2: Make it executable
chmod +x kubectl
##Step 3: Move it into PATH
sudo mv kubectl /usr/local/bin/
##Step 4: Verify installation
kubectl version --client


##Running each stage manually
# Build and test
mvn clean verify

##Build and run the container
docker build -t devops-demo-app .
docker run -p 8080:8080 devops-demo-app

## Provision AWS infrastructure
cd terraform
terraform init
terraform plan
terraform apply

##Configure hosts
cd ../ansible
ansible-playbook -i inventory.ini playbook.yml

## Deploy to Kubernetes
aws eks update-kubeconfig --name devops-demo-cluster --region ap-south-1
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get svc -n devops-demo

##Cleanup
kubectl delete namespace devops-demo
cd terraform
terraform destroy
