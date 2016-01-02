# execute 'apt-get update'
exec { 'apt-update':                    # exec resource named 'apt-update'
  command => '/usr/bin/apt-get update'  # command this resource will run
}

# install apache2 package
#package { 'apache2':
#  require => Exec['apt-update'],        # require 'apt-update' before installing
#  ensure => installed,
#}
#
## ensure apache2 service is running
#service { 'apache2':
#  ensure => running,
#}
#
## install mysql-server package
#package { 'mysql-server':
#  require => Exec['apt-update'],        # require 'apt-update' before installing
#  ensure => installed,
#}
#
## ensure mysql service is running
#service { 'mysql':
#  ensure => running,
#}
#
## ensure info.php file exists
#file { '/var/www/html/index.html':
#  ensure => file,
#  content => '<html><body>Hello world</body></html>',
#  require => Package['apache2'],        # require 'apache2' package before creating
#}COmmenting out everything except