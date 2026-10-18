//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_manifest_signature.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportManifestSignature {
  /// Returns a new [ExportManifestSignature] instance.
  ExportManifestSignature({

    required  this.alg,

    required  this.kid,

    required  this.file,
  });

  @JsonKey(
    
    name: r'alg',
    required: true,
    includeIfNull: false,
  )


  final ExportManifestSignatureAlgEnum alg;



  @JsonKey(
    
    name: r'kid',
    required: true,
    includeIfNull: false,
  )


  final String kid;



      /// manifest.json.jws
  @JsonKey(
    
    name: r'file',
    required: true,
    includeIfNull: false,
  )


  final String file;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportManifestSignature &&
      other.alg == alg &&
      other.kid == kid &&
      other.file == file;

    @override
    int get hashCode =>
        alg.hashCode +
        kid.hashCode +
        file.hashCode;

  factory ExportManifestSignature.fromJson(Map<String, dynamic> json) => _$ExportManifestSignatureFromJson(json);

  Map<String, dynamic> toJson() => _$ExportManifestSignatureToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExportManifestSignatureAlgEnum {
@JsonValue(r'Ed25519')
ed25519(r'Ed25519'),
@JsonValue(r'EdDSA')
edDSA(r'EdDSA');

const ExportManifestSignatureAlgEnum(this.value);

final String value;

@override
String toString() => value;
}


