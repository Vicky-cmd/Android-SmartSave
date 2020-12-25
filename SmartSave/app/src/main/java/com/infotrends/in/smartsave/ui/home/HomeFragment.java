package com.infotrends.in.smartsave.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.supercharge.shimmerlayout.ShimmerLayout;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.infotrends.in.smartsave.MainActivity;
import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.database.FilesAdapter;
import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.orchestrator.FilesModOrc;
import com.infotrends.in.smartsave.orchestrator.SkeletalOrc;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.FilePicker;
import com.infotrends.in.smartsave.utils.LoadingBox;
import com.infotrends.in.smartsave.utils.LoggerFile;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private Activity mActivity;
    private Context context;
    private FilesAdapter mAdapter;
    private FilesModOrc oFilesModOrc;
    private boolean uploadBtnStateOpen = false;
    private ProgressBar hscreenLoading;

    private LinearLayout skeletonLayout;
    private ShimmerLayout shimmer;
    private LayoutInflater inflater;
    private List<FileModel> fLst = new ArrayList<FileModel>();
    private SkeletalOrc skeletalOrc;
    private FloatingActionButton uploadBtn;
    private SwipeRefreshLayout swipeHomeView;
    private NavHostFragment navHostFragment;
    private NavController navController;
//    private RecyclerView recyclerView;
    private Handler handler = new Handler();
    private LoggerFile ologger = LoggerFile.createInstance(HomeFragment.class);
    private RecyclerView recyclerView;
    private FilePicker filePicker;
    private View empty_err_view;
    private String curDir = null;
    private String openedSharedDir = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel =
//                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        mActivity = getActivity();
        context = getContext();
        oFilesModOrc = new FilesModOrc();
        filePicker = new FilePicker();

        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        hscreenLoading = root.findViewById(R.id.hscreen_loading);
        /*final RecyclerView*/ recyclerView = root.findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        empty_err_view = root.findViewById(R.id.no_disp_msg_view);

        skeletonLayout = root.findViewById(R.id.skeletonLayout);
        shimmer = root.findViewById(R.id.shimmerSkeleton);
        this.inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        skeletalOrc = new SkeletalOrc(skeletonLayout, shimmer, this.inflater);

        swipeHomeView = root.findViewById(R.id.recycleview_swipe_parent_layout);
        swipeHomeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(context, "Refreashed", Toast.LENGTH_SHORT).show();
                if(curDir==null) {
                    curDir = "";
                }
                loadFiles(curDir, "N");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeHomeView.setRefreshing(false);
                    }
                }, 100);

            }
        });

        uploadBtn = root.findViewById(R.id.fab);
        final FloatingActionButton fab = root.findViewById(R.id.fab);
        final FloatingActionButton inpDoc = root.findViewById(R.id.add_doc);
        final FloatingActionButton inpPhoto = root.findViewById(R.id.add_img);
        final FloatingActionButton inpFolder = root.findViewById(R.id.add_folder);
        fab.setVisibility(View.VISIBLE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    //Scrolling down
                    uploadBtn.setVisibility(View.GONE);
                    if(uploadBtnStateOpen) {
                        inpDoc.setVisibility(View.GONE);
                        inpPhoto.setVisibility(View.GONE);
                        inpFolder.setVisibility(View.GONE);
                        uploadBtnStateOpen = false;
                    }
                } else if (dy < 0) {
                    //Scrolling up
                    uploadBtn.setVisibility(View.VISIBLE);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        inpFolder.animate().alpha(1.0f).setDuration(750);
        inpPhoto.animate().alpha(1.0f).setDuration(500);
        inpDoc.animate().alpha(1.0f).setDuration(250);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                MainActivity.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_READ_FILES);
                MainActivity.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_WRITE_FILES);
                MainActivity.checkPermission(Manifest.permission.INTERNET, MainActivity.PERMISSION_CODE_INTERNET);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!uploadBtnStateOpen) {
                            inpDoc.animate().alpha(1.0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    inpDoc.setVisibility(View.VISIBLE);
                                }
                            });
//                            inpDoc.setVisibility(View.VISIBLE);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            inpPhoto.animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    inpPhoto.setVisibility(View.VISIBLE);
                                }
                            });
//                            inpPhoto.setVisibility(View.VISIBLE);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            inpFolder.animate().alpha(1.0f).setDuration(750).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    inpFolder.setVisibility(View.VISIBLE);
                                }
                            });
                            uploadBtnStateOpen=true;
                        } else {

                            inpFolder.animate().alpha(1.0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    inpFolder.setVisibility(View.GONE);
                                }
                            });
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            inpPhoto.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    inpPhoto.setVisibility(View.GONE);
                                }
                            });
//                            inpPhoto.setVisibility(View.GONE);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            inpDoc.animate().alpha(0.0f).setDuration(750).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    inpDoc.setVisibility(View.GONE);
                                }
                            });

//                            inpDoc.setVisibility(View.GONE);
                            uploadBtnStateOpen=false;
                        }
                    }
                });
            }
        });


        inpDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filePicker.showFileChooser();
            }
        });

        inpPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filePicker.showFileChooserImages();
            }
        });

        inpFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Folder Name");
                alert.setMessage("Please Enter the Folder Name");

                final EditText input = new EditText(context);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final String folderName = input.getText().toString();
                        if(folderName.equalsIgnoreCase("")) {
                            Toast.makeText(context, "Folder Name Cannot be Empty!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if(folderName.contains("|")) {
                            Toast.makeText(context, "Folder Name Cannot Contain '|' Special Character!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Toast.makeText(context, folderName, Toast.LENGTH_SHORT).show();
                            LoadingBox.createInstance(mActivity).show();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                boolean bool = oFilesModOrc.createNewFolder(folderName);
                                LoadingBox.getInstance().dismiss();
                                if(bool) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mActivity.recreate();
                                        }
                                    });

                                }
                            }
                        }).start();
                        return;
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                return;
                            }
                        });
                alert.show();

            }
        });

        uploadBtnStateOpen = false;
//        loadFiles();
        if(getArguments()!=null) {
            if(getArguments().get("searchQuery")!=null && getArguments().getBoolean("searchQuery")) {

                if(getArguments().getString("queryString")!=null) {

//                    empty_err_view.setVisibility(View.GONE);
//                    hscreenLoading.setVisibility(View.GONE);
//                    skeletonLayout.setVisibility(View.GONE);
                    String queryTxt = getArguments().getString("queryString");
                    applySearch(queryTxt);
                    skeletalOrc.animateReplaceSkeleton(recyclerView);
                }

            } else {
                if (getArguments().getString("curDir")!=null) {
                    String currFSharedStatus = "";
                    if(getArguments().getString("curSharedFileStatus")!=null) {
                        currFSharedStatus = getArguments().getString("curSharedFileStatus");
                    }
                    curDir = getArguments().getString("curDir");
                    loadFiles(curDir, currFSharedStatus);
                } else if(getArguments().getString("openedSharedDir")!=null) {
                    openedSharedDir = getArguments().getString("openedSharedDir");
                    curDir = "";
                    loadSharedFiles(openedSharedDir);
                }else if(curDir==null) {
                    curDir = "";
                    loadFiles(curDir, "N");
                }
//                loadFiles(curDir);
            }
        } else {
            if(curDir==null) {
                curDir = "";
            }
            loadFiles(curDir, "N");
        }

        setHasOptionsMenu(true);
        AppProps.getInstance().setCurrDir(curDir);
        return root;
    }

    public void loadSharedFiles(final String curUrl) {
        empty_err_view.setVisibility(View.GONE);
        hscreenLoading.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                hscreenLoading.setVisibility(View.GONE);
                skeletalOrc.showSkeleton(true);
            }
        }, 100);

        new Thread(new Runnable() {

            @Override
            public void run() {
                fLst = oFilesModOrc.getSharedFileFromServer(curUrl);
                if(fLst!=null && fLst.size()>0) {
                    mAdapter = new FilesAdapter(context, fLst);
                } else {
                    mAdapter = new FilesAdapter(context, new ArrayList<FileModel>());
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(mAdapter);
                        skeletalOrc.animateReplaceSkeleton(recyclerView); //showSkeleton(false);
                        hscreenLoading.setVisibility(View.GONE);
                        if(fLst!=null && fLst.size()>0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    loadItemViewIcons(recyclerView);
                                }
                            }).start();
                        } else {
                            empty_err_view.setVisibility(View.VISIBLE);
                        }
                    }
                });


            }
        }).start();
    }



    public void loadFiles(final String curDir, final String currFSharedStatus) {
        empty_err_view.setVisibility(View.GONE);
        hscreenLoading.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                hscreenLoading.setVisibility(View.GONE);
                skeletalOrc.showSkeleton(true);
            }
        }, 100);

        new Thread(new Runnable() {

            @Override
            public void run() {
                fLst = oFilesModOrc.getFilesListInServer(curDir, currFSharedStatus);
                if(fLst!=null && fLst.size()>0) {
                    mAdapter = new FilesAdapter(context, fLst);
                } else {
                    mAdapter = new FilesAdapter(context, new ArrayList<FileModel>());
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(mAdapter);
                        skeletalOrc.animateReplaceSkeleton(recyclerView); //showSkeleton(false);
                        hscreenLoading.setVisibility(View.GONE);
                        if(fLst!=null && fLst.size()>0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    loadItemViewIcons(recyclerView);
                                }
                            }).start();
                        } else {
                            empty_err_view.setVisibility(View.VISIBLE);
                        }
                    }
                });


            }
        }).start();
    }

    private void loadItemViewIcons(final RecyclerView recyclerView) {
        List<FileModel> fModelLst = AppProps.getInstance().getFilesList();
        if(fModelLst!=null && fModelLst.size()>0) {
            List<Integer> posFswithoutThumb = new ArrayList<Integer>();
            for (int i = 0; i < fModelLst.size(); i++) {
                FileModel ofMod = fModelLst.get(i);
                if(ofMod.getIsImage().equalsIgnoreCase("Y")) {
                    String cacheDir = oFilesModOrc.rootPath + "/.thumbnails/";

                    File imgFile = new File(cacheDir + "/" + ofMod.getFileName());
//                    if(!imgFile.exists())
                    {
                        posFswithoutThumb.add(i);
                    }

                }
            }
            boolean bool = oFilesModOrc.getThumbnailsData(posFswithoutThumb);
            if(bool) {

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new FilesAdapter(context, AppProps.getInstance().getFilesList());
                        recyclerView.setAdapter(mAdapter);
                    }
                });

            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        mActivity.getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager =
                (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search_menu_item).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(mActivity.getComponentName()));

        searchView.setIconified(false);
        searchView.setQueryHint(getString(R.string.search_hint));
//        searchView.clearFocus();
//        searchView.onAc

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showInputMethod(view.findFocus());
                } else {
                    hideKeyboard();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                searchView.setQuery("", false);
                searchView.setIconified(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do your stuff
                ologger.info("-----------------------------------------------------" + newText + "-----------------------------------------------------");
                applySearch(newText);

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
//        return true;
    }


    private void applySearch(final String newText) {
        final List<FileModel> lstFiles = AppProps.getInstance().getFilesList();
        final List<FileModel> resFileLst = new ArrayList<FileModel>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(lstFiles!=null) {

                    empty_err_view.setVisibility(View.GONE);
                    hscreenLoading.setVisibility(View.GONE);
                    if(newText!=null && !newText.isEmpty()) {
                        for (int i = 0; i < lstFiles.size(); i++) {
                            FileModel oFileModel = lstFiles.get(i);
                            String fName = oFileModel.getFileName();
                            if (fName.toUpperCase().contains(newText.toUpperCase())) {
                                resFileLst.add(oFileModel);
                            }
                        }
                        mAdapter = new FilesAdapter(context, resFileLst);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(mAdapter);
                            }
                        });
                    } else {
                        mAdapter = new FilesAdapter(context, lstFiles);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(mAdapter);
                            }
                        });
                    }
                }
            }
        }).start();

    }

    private void applySearch1(final String newText) {
        final List<FileModel> lstFiles = AppProps.getInstance().getFilesList();
        final List<FileModel> resFileLst = new ArrayList<FileModel>();


        empty_err_view.setVisibility(View.GONE);
        hscreenLoading.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                hscreenLoading.setVisibility(View.GONE);
                skeletalOrc.showSkeleton(true);
            }
        }, 100);

        new Thread(new Runnable() {

            @Override
            public void run() {

                if(lstFiles!=null) {
                    if(newText!=null && !newText.isEmpty()) {
                        for (int i = 0; i < lstFiles.size(); i++) {
                            FileModel oFileModel = lstFiles.get(i);
                            String fName = oFileModel.getFileName();
                            if (fName.toUpperCase().contains(newText.toUpperCase())) {
                                resFileLst.add(oFileModel);
                            }
                        }
                        mAdapter = new FilesAdapter(context, resFileLst);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(mAdapter);
                                skeletalOrc.animateReplaceSkeleton(recyclerView); //showSkeleton(false);
                                hscreenLoading.setVisibility(View.GONE);
                            }
                        });

//                        recyclerView.setAdapter(mAdapter);
                    } else {
                        mAdapter = new FilesAdapter(context, lstFiles);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(mAdapter);
                                skeletalOrc.animateReplaceSkeleton(recyclerView); //showSkeleton(false);
                                hscreenLoading.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    empty_err_view.setVisibility(View.VISIBLE);
                    hscreenLoading.setVisibility(View.GONE);
                }



            }
        }).start();



    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = mActivity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(mActivity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.play_game || item.getItemId() == R.id.game_reset) {
//            resetGame.setVisible(true);
//        }
//        else {
//            resetGame.setVisible(false);
//        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                return false;
            case R.id.search_menu_item:
                return true;
            case R.id.action_logout:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
