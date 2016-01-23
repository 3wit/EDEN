package com.example.ejwessel.eden;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks
{

  //get token here
  private String clientToke = null;


  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */
  private NavigationDrawerFragment mNavigationDrawerFragment;

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mNavigationDrawerFragment = (NavigationDrawerFragment)
            getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mTitle = getTitle();

    WebView mWebView = (WebView) findViewById(R.id.webview);
    mWebView.loadUrl("http://ewit.me/eden/");


    // Set up the drawer.
    mNavigationDrawerFragment.setUp(
            R.id.navigation_drawer,
            (DrawerLayout) findViewById(R.id.drawer_layout));

    //register receiver for when user is present
    registerReceiver(
            new UserPresentReceiver(),
            new IntentFilter("android.intent.action.USER_PRESENT"));
    AsyncHttpClient client = new AsyncHttpClient();
    client.get("https://eden-bt.herokuapp.com/client_token", new TextHttpResponseHandler()
    {
      @Override
      public void onSuccess(int statusCode, Header[] headers, String clientToken)
      {
        clientToke = clientToken;
      }

      @Override
      public void onStart()
      {
        // called before request is started
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, String s, Throwable e)
      {
        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
      }

      @Override
      public void onRetry(int retryNo)
      {
        // called when request is retried
      }
    });
  }

  @Override
  public void onNavigationDrawerItemSelected(int position)
  {
    Fragment fragment;

    switch(position){
      case 0:
        fragment = new PlaceholderFragment();
        System.out.println("User Account Info");
        break;
      case 1:
        fragment = new PlaceholderFragment();
        System.out.println("Setup Subscription");
        onBraintreeSubmit();
        break;
      case 2:
        System.out.println("Check-In");
        Intent i = new Intent();
        i.setClassName("com.example.ejwessel.eden", "com.example.ejwessel.eden.CameraActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(i);
        break;
    }

    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
            .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
            .commit();
  }

  public void onSectionAttached(int number)
  {
    switch (number) {
      case 1:
        mTitle = getString(R.string.title_section1);
        break;
      case 2:
        mTitle = getString(R.string.title_section2);
        break;
      case 3:
        mTitle = getString(R.string.title_section3);
        break;
    }
  }

  public void restoreActionBar()
  {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment
  {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber)
    {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      return rootView;
    }

    @Override
    public void onAttach(Activity activity)
    {
      super.onAttach(activity);
      ((MainActivity) activity).onSectionAttached(
              getArguments().getInt(ARG_SECTION_NUMBER));
    }
  }

  public void onBraintreeSubmit() {
    PaymentRequest paymentRequest = new PaymentRequest()
              .clientToken(clientToke)
            .primaryDescription("Setup Subscription")
            .secondaryDescription("Monthly Subscription")
            .amount("$5")
            .submitButtonText("Purchase")
            .androidPayRequestCode(100);
      startActivityForResult(paymentRequest.getIntent(this), 100);


  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 100) {
      switch (resultCode) {
        case Activity.RESULT_OK:
          PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(
                  BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
          );
          String nonce = paymentMethodNonce.getNonce();
          Toast.makeText(this, "You have been subscribed", Toast.LENGTH_LONG).show();
          break;
        case BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR:
        case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR:
        case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE:
          // handle errors here, a throwable may be available in
          // data.getSerializableExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE)
          break;
        default:
          break;
      }
    }
  }
}
