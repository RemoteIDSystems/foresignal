//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_corroboration_auto_corroborate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCorroborationAutoCorroborate {
  /// Returns a new [IntelCorroborationAutoCorroborate] instance.
  IntelCorroborationAutoCorroborate({

     this.enabled,

     this.withinKm,

     this.window,

     this.maxMembers,
  });

      /// Whether a lone event searches for corroboration on creation (default true)
  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// The search radius around the event's members, in kilometers (default 50)
          // minimum: 0
  @JsonKey(
    
    name: r'withinKm',
    required: false,
    includeIfNull: false,
  )


  final num? withinKm;



      /// The time window before and after the members' observation span, an ISO-8601 or shorthand duration (default PT24H)
  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final String? window;



      /// The most corroborating members one search attaches, one per independent originator root (default 10)
          // minimum: 1
  @JsonKey(
    
    name: r'maxMembers',
    required: false,
    includeIfNull: false,
  )


  final int? maxMembers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelCorroborationAutoCorroborate &&
      other.enabled == enabled &&
      other.withinKm == withinKm &&
      other.window == window &&
      other.maxMembers == maxMembers;

    @override
    int get hashCode =>
        enabled.hashCode +
        withinKm.hashCode +
        window.hashCode +
        maxMembers.hashCode;

  factory IntelCorroborationAutoCorroborate.fromJson(Map<String, dynamic> json) => _$IntelCorroborationAutoCorroborateFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCorroborationAutoCorroborateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

