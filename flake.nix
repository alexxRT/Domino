{
  description = "Domino game Java client/server project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        jdk = (pkgs.openjdk11.override { enableJavaFX = true; }); 
        maven = pkgs.maven;
      in {
        devShells.default = pkgs.mkShell {
          packages = [ jdk maven ];
          JAVA_HOME = "${jdk}";
        };
      }
    );

}

