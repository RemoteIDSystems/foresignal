//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/settings_warning.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_security.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoSecurity {
  /// Returns a new [SystemInfoSecurity] instance.
  SystemInfoSecurity({

     this.openReads,

     this.anonymousRestricted,

     this.warnings,
  });

  @JsonKey(
    
    name: r'openReads',
    required: false,
    includeIfNull: false,
  )


  final bool? openReads;



      /// True when the anonymous profile restricts anything beyond its defaults
  @JsonKey(
    
    name: r'anonymousRestricted',
    required: false,
    includeIfNull: false,
  )


  final bool? anonymousRestricted;



      /// PUBLIC_BASE_URL_INSECURE, PUBLIC_BASE_URL_MISMATCH (023), ANONYMOUS_UNRESTRICTED (035)
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<SettingsWarning>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoSecurity &&
      other.openReads == openReads &&
      other.anonymousRestricted == anonymousRestricted &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        openReads.hashCode +
        anonymousRestricted.hashCode +
        warnings.hashCode;

  factory SystemInfoSecurity.fromJson(Map<String, dynamic> json) => _$SystemInfoSecurityFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoSecurityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

