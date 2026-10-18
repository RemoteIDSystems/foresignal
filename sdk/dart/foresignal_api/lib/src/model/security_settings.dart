//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'security_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SecuritySettings {
  /// Returns a new [SecuritySettings] instance.
  SecuritySettings({

     this.openReads = true,

     this.exposeObserver = false,

     this.anonymous,

     this.metricsOpen = true,

     this.exposeMlat = false,
  });

      /// When false, data GETs require a read token
  @JsonKey(
    defaultValue: true,
    name: r'openReads',
    required: true,
    includeIfNull: false,
  )


  final bool openReads;



      /// Return record observers to read-scope and anonymous callers with include=observer
  @JsonKey(
    defaultValue: false,
    name: r'exposeObserver',
    required: false,
    includeIfNull: false,
  )


  final bool? exposeObserver;



      /// Anonymous restriction profile applied to tokenless reads while openReads is true (035). Default: no source/kind/area limits, private excluded, data GETs only (never inbox, places, watchlists or alwaysAuthenticated operations), 120 requests/min
  @JsonKey(
    
    name: r'anonymous',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? anonymous;



      /// When false, /actuator/prometheus requires an admin token (035)
  @JsonKey(
    defaultValue: true,
    name: r'metricsOpen',
    required: false,
    includeIfNull: false,
  )


  final bool? metricsOpen;



      /// When false (the default), records whose `posSource` is `mlat` are stripped from anonymous openReads responses of the records API (list, get, versions, history, asOf); a record whose `posSource` was projected away by `fields=` is stripped too when its source reports `posSource`. MLAT positions are never sent on sinks, federation, share links or feeds regardless (069)
  @JsonKey(
    defaultValue: false,
    name: r'exposeMlat',
    required: false,
    includeIfNull: false,
  )


  final bool? exposeMlat;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SecuritySettings &&
      other.openReads == openReads &&
      other.exposeObserver == exposeObserver &&
      other.anonymous == anonymous &&
      other.metricsOpen == metricsOpen &&
      other.exposeMlat == exposeMlat;

    @override
    int get hashCode =>
        openReads.hashCode +
        exposeObserver.hashCode +
        anonymous.hashCode +
        metricsOpen.hashCode +
        exposeMlat.hashCode;

  factory SecuritySettings.fromJson(Map<String, dynamic> json) => _$SecuritySettingsFromJson(json);

  Map<String, dynamic> toJson() => _$SecuritySettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

