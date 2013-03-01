%global plugindir   /var/teamcity/server/plugins

Summary:        TeamCity plugin for building checking ABI compatibility with previous builds
Name:           teamcity-abi-checker
Version:        0.2
Release:        2.tc7%{?dist}
Group:          Development/Tools/Other
Source:         %{name}-%{version}.tar.gz
License:        GPL3
Packager:       Justin Salmon <jsalmon@cern.ch>
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
BuildRequires:  ant >= 1.7.1
BuildRequires:  java-1.6.0-openjdk
BuildRequires:  teamcity-server >= 7.0.1

%description
TeamCity plugin for building checking ABI compatibility with previous builds

%prep
%setup

%build
ant dist -Dteamcity.distribution=/opt/teamcity/server -Dant.build.javac.source=1.6 -Dant.build.javac.target=1.6

%install
mkdir -p %{buildroot}%plugindir
install -pm 755 dist/abi-checker.zip %{buildroot}/%plugindir

%files
%plugindir/abi-checker.zip
