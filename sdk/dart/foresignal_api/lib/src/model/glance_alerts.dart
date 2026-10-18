//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/glance_alerts_open.dart';
import 'package:foresignal_api/src/model/glance_alerts_latest_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance_alerts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GlanceAlerts {
  /// Returns a new [GlanceAlerts] instance.
  GlanceAlerts({

     this.open,

     this.latest,
  });

  @JsonKey(
    
    name: r'open',
    required: false,
    includeIfNull: false,
  )


  final GlanceAlertsOpen? open;



  @JsonKey(
    
    name: r'latest',
    required: false,
    includeIfNull: false,
  )


  final List<GlanceAlertsLatestInner>? latest;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GlanceAlerts &&
      other.open == open &&
      other.latest == latest;

    @override
    int get hashCode =>
        open.hashCode +
        latest.hashCode;

  factory GlanceAlerts.fromJson(Map<String, dynamic> json) => _$GlanceAlertsFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceAlertsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

