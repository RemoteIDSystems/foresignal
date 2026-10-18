//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_sinks.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoSinks {
  /// Returns a new [SystemInfoSinks] instance.
  SystemInfoSinks({

     this.enabled,

     this.failing,
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final int? enabled;



  @JsonKey(
    
    name: r'failing',
    required: false,
    includeIfNull: false,
  )


  final int? failing;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoSinks &&
      other.enabled == enabled &&
      other.failing == failing;

    @override
    int get hashCode =>
        enabled.hashCode +
        failing.hashCode;

  factory SystemInfoSinks.fromJson(Map<String, dynamic> json) => _$SystemInfoSinksFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoSinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

