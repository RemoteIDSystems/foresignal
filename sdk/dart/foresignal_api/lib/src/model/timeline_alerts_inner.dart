//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'timeline_alerts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TimelineAlertsInner {
  /// Returns a new [TimelineAlertsInner] instance.
  TimelineAlertsInner({

    required  this.t,

     this.info,

     this.warning,

     this.critical,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'info',
    required: false,
    includeIfNull: false,
  )


  final int? info;



  @JsonKey(
    
    name: r'warning',
    required: false,
    includeIfNull: false,
  )


  final int? warning;



  @JsonKey(
    
    name: r'critical',
    required: false,
    includeIfNull: false,
  )


  final int? critical;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TimelineAlertsInner &&
      other.t == t &&
      other.info == info &&
      other.warning == warning &&
      other.critical == critical;

    @override
    int get hashCode =>
        t.hashCode +
        info.hashCode +
        warning.hashCode +
        critical.hashCode;

  factory TimelineAlertsInner.fromJson(Map<String, dynamic> json) => _$TimelineAlertsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TimelineAlertsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

