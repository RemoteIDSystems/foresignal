//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/resource_thresholds.dart';
import 'package:foresignal_api/src/model/smtp_settings_input.dart';
import 'package:foresignal_api/src/model/setup_request_admin.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'setup_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SetupRequest {
  /// Returns a new [SetupRequest] instance.
  SetupRequest({

    required  this.admin,

     this.smtp,

     this.sources,

     this.thresholds,

     this.retention,
  });

  @JsonKey(
    
    name: r'admin',
    required: true,
    includeIfNull: false,
  )


  final SetupRequestAdmin admin;



  @JsonKey(
    
    name: r'smtp',
    required: false,
    includeIfNull: false,
  )


  final SmtpSettingsInput? smtp;



      /// Source ids to enable in the collection set; each must be a registered source
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'thresholds',
    required: false,
    includeIfNull: false,
  )


  final ResourceThresholds? thresholds;



      /// Optional retention policy to seed
  @JsonKey(
    
    name: r'retention',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? retention;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SetupRequest &&
      other.admin == admin &&
      other.smtp == smtp &&
      other.sources == sources &&
      other.thresholds == thresholds &&
      other.retention == retention;

    @override
    int get hashCode =>
        admin.hashCode +
        (smtp == null ? 0 : smtp.hashCode) +
        sources.hashCode +
        (thresholds == null ? 0 : thresholds.hashCode) +
        (retention == null ? 0 : retention.hashCode);

  factory SetupRequest.fromJson(Map<String, dynamic> json) => _$SetupRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SetupRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

