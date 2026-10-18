//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_retract_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportRetractRequest {
  /// Returns a new [ReportRetractRequest] instance.
  ReportRetractRequest({

     this.revision,

    required  this.reason,
  });

      /// Default: latest
  @JsonKey(
    
    name: r'revision',
    required: false,
    includeIfNull: false,
  )


  final int? revision;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportRetractRequest &&
      other.revision == revision &&
      other.reason == reason;

    @override
    int get hashCode =>
        (revision == null ? 0 : revision.hashCode) +
        reason.hashCode;

  factory ReportRetractRequest.fromJson(Map<String, dynamic> json) => _$ReportRetractRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ReportRetractRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

