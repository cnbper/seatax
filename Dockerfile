FROM sloth.com:5000/cmf/base-java9:latest

ADD target/cmf-paas-access-1.0.0-linux.tar.gz /usr/local/

CMD ["/usr/local/cmf-paas-access-1.0.0/bin/access", "start"]