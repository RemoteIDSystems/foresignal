//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/jwk.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'jwks.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Jwks {
  /// Returns a new [Jwks] instance.
  Jwks({

    required  this.keys,
  });

  @JsonKey(
    
    name: r'keys',
    required: true,
    includeIfNull: false,
  )


  final List<Jwk> keys;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Jwks &&
      other.keys == keys;

    @override
    int get hashCode =>
        keys.hashCode;

  factory Jwks.fromJson(Map<String, dynamic> json) => _$JwksFromJson(json);

  Map<String, dynamic> toJson() => _$JwksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

