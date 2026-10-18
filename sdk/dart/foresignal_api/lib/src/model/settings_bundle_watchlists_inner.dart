//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/watchlist.dart';
import 'package:foresignal_api/src/model/watchlist_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'settings_bundle_watchlists_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SettingsBundleWatchlistsInner {
  /// Returns a new [SettingsBundleWatchlistsInner] instance.
  SettingsBundleWatchlistsInner({

     this.watchlist,

     this.items,
  });

  @JsonKey(
    
    name: r'watchlist',
    required: false,
    includeIfNull: false,
  )


  final Watchlist? watchlist;



  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<WatchlistItem>? items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SettingsBundleWatchlistsInner &&
      other.watchlist == watchlist &&
      other.items == items;

    @override
    int get hashCode =>
        watchlist.hashCode +
        items.hashCode;

  factory SettingsBundleWatchlistsInner.fromJson(Map<String, dynamic> json) => _$SettingsBundleWatchlistsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SettingsBundleWatchlistsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

