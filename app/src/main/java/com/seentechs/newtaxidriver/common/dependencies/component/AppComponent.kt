package com.seentechs.newtaxidriver.common.dependencies.component

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage dependencies.component
 * @category AppComponent
 * @author Seen Technologies
 *
 */

import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.dependencies.module.AppContainerModule
import com.seentechs.newtaxidriver.common.dependencies.module.ApplicationModule
import com.seentechs.newtaxidriver.common.dependencies.module.ImageCompressAsyncTask
import com.seentechs.newtaxidriver.common.dependencies.module.NetworkModule
import com.seentechs.newtaxidriver.common.helper.CarTypeAdapter
import com.seentechs.newtaxidriver.common.helper.CommonDialog
import com.seentechs.newtaxidriver.common.helper.RunTimePermission
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoice
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.common.views.PaymentWebViewActivity
import com.seentechs.newtaxidriver.common.views.SupportActivityCommon
import com.seentechs.newtaxidriver.common.views.SupportAdapter
import com.seentechs.newtaxidriver.google.direction.GetDirectionData
import com.seentechs.newtaxidriver.google.locationmanager.*
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.facebookAccountKit.FacebookAccountKitActivity
import com.seentechs.newtaxidriver.home.firebaseChat.ActivityChat
import com.seentechs.newtaxidriver.home.firebaseChat.AdapterFirebaseRecylcerview
import com.seentechs.newtaxidriver.home.firebaseChat.FirebaseChatHandler
import com.seentechs.newtaxidriver.home.fragments.AccountFragment
import com.seentechs.newtaxidriver.home.fragments.EarningActivity
import com.seentechs.newtaxidriver.home.fragments.HomeFragment
import com.seentechs.newtaxidriver.home.fragments.RatingActivity
import com.seentechs.newtaxidriver.home.fragments.Referral.ShowReferralOptionsActivity
import com.seentechs.newtaxidriver.home.fragments.currency.CurrencyListAdapter
import com.seentechs.newtaxidriver.home.fragments.language.LanguageAdapter
import com.seentechs.newtaxidriver.home.fragments.payment.*
import com.seentechs.newtaxidriver.home.managevehicles.*
import com.seentechs.newtaxidriver.home.managevehicles.adapter.*
import com.seentechs.newtaxidriver.home.map.GpsService
import com.seentechs.newtaxidriver.home.map.drawpolyline.DownloadTask
import com.seentechs.newtaxidriver.home.paymentstatement.*
import com.seentechs.newtaxidriver.home.payouts.*
import com.seentechs.newtaxidriver.home.payouts.adapter.PayoutCountryListAdapter
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.PayPalEmailAdapter
import com.seentechs.newtaxidriver.home.profile.DriverProfile
import com.seentechs.newtaxidriver.home.profile.VehiclInformation
import com.seentechs.newtaxidriver.home.pushnotification.MyFirebaseInstanceIDService
import com.seentechs.newtaxidriver.home.pushnotification.MyFirebaseMessagingService
import com.seentechs.newtaxidriver.home.service.ForeService
import com.seentechs.newtaxidriver.home.service.LocationService
import com.seentechs.newtaxidriver.home.signinsignup.*
import com.seentechs.newtaxidriver.home.splash.SplashActivity
import com.seentechs.newtaxidriver.trips.*
import com.seentechs.newtaxidriver.trips.rating.*
import com.seentechs.newtaxidriver.trips.tripsdetails.*
import com.seentechs.newtaxidriver.trips.viewmodel.ReqAccpVM
import com.seentechs.newtaxidriver.trips.voip.CallProcessingActivity
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService
import dagger.Component
import javax.inject.Singleton


/*****************************************************************
 * App Component
 */
@Singleton
@Component(modules = [NetworkModule::class, ApplicationModule::class, AppContainerModule::class])
interface AppComponent {
    // ACTIVITY

    fun inject(bankDetailsActivity: BankDetailsActivity)


    fun inject(currencyListAdapter: CurrencyListAdapter)

    fun inject(payoutEmailActivity: PayoutEmailActivity)

    fun inject(payoutEmailListActivity: PayoutEmailListActivity)


    fun inject(payPalEmailAdapter: PayPalEmailAdapter)

    fun inject(payoutAddressDetailsActivity: PayoutAddressDetailsActivity)

    fun inject(payoutBankDetailsActivity: PayoutBankDetailsActivity)

    fun inject(payoutCoutryListAdapter2: PayoutCoutryListAdapter2)

    fun inject(priceStatementAdapter: PriceStatementAdapter)

    fun inject(paymentPage: PaymentPage)

    fun inject(driverDetailsAdapter: DriverDetailsAdapter)

    fun inject(sessionManager: SessionManager)

    fun inject(pendingTripsFragment: PendingTripsFragment)

    fun inject(accountFragment: AccountFragment)

    fun inject(viewDocumentFragment: ViewVehicleDocumentFragment)

    fun inject(homeFragment: HomeFragment)

    fun inject(past: CompletedTripsFragments)

    fun inject(ratingFragment: RatingActivity)

    fun inject(comments: Comments)

    fun inject(yourTrips: YourTrips)

    fun inject(carTypeAdapter: CarTypeAdapter)

    fun inject(tripDetails: TripDetails)

    fun inject(PaymentStatementActivity: PaymentStatementActivity)


    fun inject(ManageVehicleActivity: ManageVehicleFragment)
    fun inject(vehicleTypeAdapter: VehicleTypeAdapter)

    fun inject(earningFragment: EarningActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(signinSignupHomeActivity: SigninSignupHomeActivity)

    fun inject(splashActivity: SplashActivity)

    fun inject(addPayment: AddPayment)

    fun inject(riderProfilePage: RiderProfilePage)

    fun inject(setting_Activity: SettingActivity)

    fun inject(requestReceiveActivity: RequestReceiveActivity)

    fun inject(manageDriverDocFrag: ManageDriverDocumentFragment)

    fun inject(viewVehicleDocFrag: ViewDriverDocumentFragment)

    fun inject(requestAcceptActivity: RequestAcceptActivity)

    fun inject(riderContactActivity: RiderContactActivity)

    fun inject(cancelYourTripActivity: CancelYourTripActivity)

    fun inject(documentDetails: DocumentDetails)

    fun inject(paymentAmountPage: PaymentAmountPage)

    fun inject(payStatementDetails: PayStatementDetails)

    fun inject(tripEarningsDetail: TripEarningsDetail)

    fun inject(dailyEarningDetails: DailyEarningDetails)

    fun inject(riderrating: Riderrating)

    fun inject(gps_service: GpsService)

    fun inject(registerCarDetailsActivity: RegisterCarDetailsActivity)

    fun inject(resetPassword: ResetPassword)

    fun inject(register: Register)

    fun inject(registerOTPActivity: RegisterOTPActivity)

    fun inject(commonMethods: CommonMethods)

    fun inject(MobileActivity: MobileActivity)

    fun inject(signinActivity: SigninActivity)

    fun inject(requestCallback: RequestCallback)

    fun inject(runTimePermission: RunTimePermission)

    fun inject(driverProfile: DriverProfile)

    fun inject(vehiclInformation: VehiclInformation)

    fun inject(riderFeedBack: RiderFeedBack)

    fun inject(activityChat: ActivityChat)

    fun inject(facebookAccountKitActivity: FacebookAccountKitActivity)

    // Adapters
    fun inject(manageVehicleAdapter: ManageVehicleAdapter)

    fun inject(yearAdapter: YearAdapter)

    fun inject(payoutDetailsListAdapter: PayoutDetailsListAdapter)

    fun inject(languageAdapter: LanguageAdapter)

    fun inject(addVehicle: AddVehicleFragment)

    fun inject(manageDocumentsAdapter: ManageDocumentsAdapter)

    fun inject(myFirebaseMessagingService: MyFirebaseMessagingService)

    fun inject(myFirebaseInstanceIDService: MyFirebaseInstanceIDService)

    fun inject(imageCompressAsyncTask: ImageCompressAsyncTask)

    fun inject(firebaseChatHandler: FirebaseChatHandler)

    fun inject(payoutCountryListAdapter: PayoutCountryListAdapter)

    fun inject(adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview)

    fun inject(makeAdapter: MakeAdapter)

    fun inject(modelAdapter: ModelAdapter)

    fun inject(reqAccpVM: ReqAccpVM)

    //    service

    fun inject(downloadTask: DownloadTask)

    fun inject(foreService: ForeService)


    fun inject(newTaxiSinchService: NewTaxiSinchService)

    //fun inject(workerUtils: WorkerUtils)


    //fun inject(updateGPSWorker: UpdateGPSWorker)


    fun inject(locationService: LocationService)

    fun inject(firebaseDatabase: AddFirebaseDatabase)

    fun inject(payToAdminActivity: PayToAdminActivity)

    fun inject(manageVehicles: ManageVehicles)

    fun inject(paymentActivity: PaymentActivity)

    fun inject(addCardActivity: AddCardActivity)

    fun inject(payoutDetailsListActivity: PayoutDetailsListActivity)

    fun inject(manageDocumentActivity: ManageVehicleDocumentFragment)

    fun inject(callProcessingActivity: CallProcessingActivity)

    fun inject(priceRecycleAdapter: PriceRecycleAdapter)

    fun inject(showReferralOptionsActivity: ShowReferralOptionsActivity)

    fun inject(upcomingTripsPaginationAdapter: PendingTripsPaginationAdapter)

    fun inject(pastTripsPaginationAdapter: CompletedTripsPaginationAdapter)
    fun inject(paymentMethodAdapter: PaymentMethodAdapter)


    fun inject(featuresInVehicleAdapter: FeaturesInVehicleAdapter)


    /**
     * Live Tracking Injects
     */
    fun inject(getDirectionData: GetDirectionData)

    fun inject(updateLocations: UpdateLocations)

    fun inject(trackingServiceListener: TrackingServiceListener)

    fun inject(trackingService: TrackingService)

    fun inject(androidPositionProvider: AndroidPositionProvider)

    fun inject(trackingController: TrackingController)

    fun inject(supportActivityCommon: SupportActivityCommon)

    fun inject(supportAdapter: SupportAdapter)

    fun inject(payStatementPaginationAdapter: PayStatementPaginationAdapter)

    fun inject(dailyEarnPaginationAdapter: DailyEarnPaginationAdapter)

    fun inject(dailyHoursPaginationAdapter: DailyHoursPaginationAdapter)

    fun inject(dailyEarnListAdapter: DailyEarnListAdapter)

    fun inject(paymentWebViewActivity: PaymentWebViewActivity)

    fun inject(commonActivity: CommonActivity)

    fun inject(commonDialog: CommonDialog)

    fun inject(commentsPaginationAdapter: CommentsPaginationAdapter)

    fun inject(userChoice: UserChoice)

    fun inject(commentsRecycleAdapter: CommentsRecycleAdapter)

    //fun inject(applicationContext: Context)

}
