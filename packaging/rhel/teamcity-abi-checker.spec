%global plugindir   /var/teamcity/.BuildServer/plugins

Summary:        TeamCity plugin for building checking ABI compatibility with previous builds
Name:           teamcity-abi-checker
Version:        0.2
Release:        1.tc7%{?dist}
Group:          Development/Tools/Other
Source:         %{name}-%{version}.tar.gz
License:        GPL3
Packager:       Justin Salmon <jsalmon@cern.ch>
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
BuildRequires:  ant >= 1.7.1

%description
TeamCity plugin for building checking ABI compatibility with previous builds

%prep
%setup

%build
ant dist

%install
mkdir -p %{buildroot}%plugindir
install -pm 755 dist/abi-checker.zip %{buildroot}/%plugindir

%files
%plugindir/abi-checker.zip
