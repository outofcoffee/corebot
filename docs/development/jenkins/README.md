# Jenkins

API docs: https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API

## Trigger parameterised job

    curl -X POST JENKINS_URL/job/JOB_NAME/buildWithParameters \
      --data token=TOKEN \
      --data-urlencode id='123'
