//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'credentials_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CredentialsUpdate {
  /// Returns a new [CredentialsUpdate] instance.
  CredentialsUpdate({

    required  this.values,
  });

      /// Keys match the descriptor's credentialFields
  @JsonKey(
    
    name: r'values',
    required: true,
    includeIfNull: false,
  )


  final Map<String, String> values;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CredentialsUpdate &&
      other.values == values;

    @override
    int get hashCode =>
        values.hashCode;

  factory CredentialsUpdate.fromJson(Map<String, dynamic> json) => _$CredentialsUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$CredentialsUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

