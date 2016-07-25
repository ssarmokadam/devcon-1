package com.devonfw.devcon.modules.github;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.devonfw.devcon.common.api.annotations.CmdModuleRegistry;
import com.devonfw.devcon.common.api.annotations.Command;
import com.devonfw.devcon.common.api.annotations.Parameter;
import com.devonfw.devcon.common.api.annotations.Parameters;
import com.devonfw.devcon.common.api.data.ContextType;
import com.devonfw.devcon.common.impl.AbstractCommandModule;

/**
 * This class contains command to clone oasp4j and devon repositories.
 *
 * @author ssarmoka
 */
@CmdModuleRegistry(name = "github", description = "Module to get Github repositories related to Devonfw.")
public class Github extends AbstractCommandModule {

  final String OASP4J_URL = "https://github.com/oasp/oasp4j.git";

  final String DEVON_URL = "https://github.com/devonfw/devon.git";

  final String PROXY_HOST = "1.0.5.10";

  final int PROXY_PORT = 8080;

  /**
   * This command is to clone oasp4j repository.
   *
   * @param path location to download the oasp4j repository.
   * @throws Exception
   */
  @Command(name = "oasp4j", description = "This command clones oasp4j repository.", context = ContextType.NONE)
  @Parameters(values = { @Parameter(name = "path", description = "a location for the oasp4j download (Current directory if not provided)", optional = true) })
  public void oasp4j(String path) throws Exception {

    path = path.isEmpty() ? this.contextPathInfo.getCurrentWorkingDirectory().toString() : path;

    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdirs();
    }

    getOutput().showMessage("Cloning from " + this.OASP4J_URL + " to " + path);

    try {
      setProxyForGithub();
      Git result = Git.cloneRepository().setURI(this.OASP4J_URL).setDirectory(folder).call();
      getOutput().showMessage("Having repository: " + result.getRepository().getDirectory());

    } catch (Exception e) {
      getOutput().showError("Getting the OASP4J code from Github.");
      throw e;
    }

  }

  /**
   * This command clones devon ditribution. This requires authentication as devon is private repository.
   *
   * @param path
   * @param username
   * @param password
   * @throws Exception
   */
  @Command(name = "devoncode", description = "This command clones devon repository.", context = ContextType.NONE)
  @Parameters(values = {
  @Parameter(name = "path", description = "a location for the devon download (Current directory if not provided)", optional = true),
  @Parameter(name = "username", description = "a user with permissions to download the Devon repository from Github."),
  @Parameter(name = "password", description = "the password related to the user with permissions to download the Devon repository."), })
  public void devoncode(String path, String username, String password) throws Exception {

    path = path.isEmpty() ? this.contextPathInfo.getCurrentWorkingDirectory().toString() : path;

    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdirs();
    }

    getOutput().showMessage("Cloning from " + this.DEVON_URL + " to " + path);

    try {
      setProxyForGithub();
      Git result =
          Git.cloneRepository().setURI(this.DEVON_URL).setDirectory(folder)
              .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();

      getOutput().showMessage("Having repository: " + result.getRepository().getDirectory());
    } catch (Exception e) {
      getOutput().showError("Getting the Devonfw code from Github.");
      throw e;
    }

  }

  private void setProxyForGithub() {

    ProxySelector.setDefault(new ProxySelector() {
      final ProxySelector delegate = ProxySelector.getDefault();

      @Override
      public List<Proxy> select(URI uri) {

        if (uri.toString().contains("github") && uri.toString().contains("https")) {
          return Arrays.asList(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(Github.this.PROXY_HOST,
              Github.this.PROXY_PORT)));
        }
        if (uri.toString().contains("github") && uri.toString().contains("http")) {
          return Arrays.asList(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(Github.this.PROXY_HOST,
              Github.this.PROXY_PORT)));
        }

        return this.delegate == null ? Arrays.asList(Proxy.NO_PROXY) : this.delegate.select(uri);
      }

      @Override
      public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

        if (uri == null || sa == null || ioe == null) {
          throw new IllegalArgumentException("Arguments can't be null.");
        }
      }
    });
  }
}