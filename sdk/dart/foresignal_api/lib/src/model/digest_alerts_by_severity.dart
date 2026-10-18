//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_alerts_by_severity.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestAlertsBySeverity {
  /// Returns a new [DigestAlertsBySeverity] instance.
  DigestAlertsBySeverity({

     this.critical,

     this.warning,

     this.info,
  });

  @JsonKey(
    
    name: r'critical',
    required: false,
    includeIfNull: false,
  )


  final int? critical;



  @JsonKey(
    
    name: r'warning',
    required: false,
    includeIfNull: false,
  )


  final int? warning;



  @JsonKey(
    
    name: r'info',
    required: false,
    includeIfNull: false,
  )


  final int? info;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestAlertsBySeverity &&
      other.critical == critical &&
      other.warning == warning &&
      other.info == info;

    @override
    int get hashCode =>
        critical.hashCode +
        warning.hashCode +
        info.hashCode;

  factory DigestAlertsBySeverity.fromJson(Map<String, dynamic> json) => _$DigestAlertsBySeverityFromJson(json);

  Map<String, dynamic> toJson() => _$DigestAlertsBySeverityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

