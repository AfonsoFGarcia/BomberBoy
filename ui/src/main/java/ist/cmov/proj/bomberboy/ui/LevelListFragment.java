package ist.cmov.proj.bomberboy.ui;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duarte on 07-05-2014.
 */
public class LevelListFragment extends ListFragment {

    private ArrayList<Integer> levels = new ArrayList<Integer>();
    View mContentView = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        levels.add(R.raw.l1);
        levels.add(R.raw.l1_demo);
        levels.add(R.raw.l2_demo);
        levels.add(R.raw.l3_demo);
        this.setListAdapter(new LevelListAdapter(getActivity(), R.layout.row_level, levels));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.level_list, null);
        return mContentView;
    }


    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Integer level = (Integer) getListAdapter().getItem(position);
        Intent i = new Intent(getActivity(), Main.class);
        i.putExtra("LEVEL", level);
        startActivity(i);
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class LevelListAdapter extends ArrayAdapter<Integer> {

        private List<Integer> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public LevelListAdapter(Context context, int textViewResourceId,
                                List<Integer> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_level, null);
            }
            Integer level = items.get(position);
            if (level != null) {
                TextView top;
                if (level == R.raw.l1) {
                    top = (TextView) v.findViewById(R.id.level_name);
                    top.setText("Our Level");
                }
                if (level == R.raw.l1_demo) {
                    top = (TextView) v.findViewById(R.id.level_name);
                    top.setText("Demo Level 1");
                }
                if (level == R.raw.l2_demo) {
                    top = (TextView) v.findViewById(R.id.level_name);
                    top.setText("Demo Level 2");

                }
                if (level == R.raw.l3_demo) {
                    top = (TextView) v.findViewById(R.id.level_name);
                    top.setText("Demo Level 3");
                }
            }
            return v;
        }
    }

}
