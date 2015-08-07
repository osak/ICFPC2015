FROM ubuntu:14.04

RUN apt-get update
RUN apt-get install -y nginx
ADD nginx.conf /etc/nginx/conf.d/
RUN echo "\ndaemon off;" >> /etc/nginx/nginx.conf
RUN rm /etc/nginx/sites-enabled/default
WORKDIR /etc/nginx
CMD ["nginx"]
EXPOSE 80
