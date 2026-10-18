//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_alerts_by_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestAlertsByStatus {
  /// Returns a new [DigestAlertsByStatus] instance.
  DigestAlertsByStatus({

     this.firing,

     this.acknowledged,

     this.resolved,
  });

  @JsonKey(
    
    name: r'firing',
    required: false,
    includeIfNull: false,
  )


  final int? firing;



  @JsonKey(
    
    name: r'acknowledged',
    required: false,
    includeIfNull: false,
  )


  final int? acknowledged;



  @JsonKey(
    
    name: r'resolved',
    required: false,
    includeIfNull: false,
  )


  final int? resolved;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestAlertsByStatus &&
      other.firing == firing &&
      other.acknowledged == acknowledged &&
      other.resolved == resolved;

    @override
    int get hashCode =>
        firing.hashCode +
        acknowledged.hashCode +
        resolved.hashCode;

  factory DigestAlertsByStatus.fromJson(Map<String, dynamic> json) => _$DigestAlertsByStatusFromJson(json);

  Map<String, dynamic> toJson() => _$DigestAlertsByStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

