// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_manifest_signature.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportManifestSignatureCWProxy {
  ExportManifestSignature alg(ExportManifestSignatureAlgEnum alg);

  ExportManifestSignature kid(String kid);

  ExportManifestSignature file(String file);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportManifestSignature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportManifestSignature(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportManifestSignature call({
    ExportManifestSignatureAlgEnum alg,
    String kid,
    String file,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportManifestSignature.copyWith(...)` or call `instanceOfExportManifestSignature.copyWith.fieldName(value)` for a single field.
class _$ExportManifestSignatureCWProxyImpl
    implements _$ExportManifestSignatureCWProxy {
  const _$ExportManifestSignatureCWProxyImpl(this._value);

  final ExportManifestSignature _value;

  @override
  ExportManifestSignature alg(ExportManifestSignatureAlgEnum alg) =>
      call(alg: alg);

  @override
  ExportManifestSignature kid(String kid) => call(kid: kid);

  @override
  ExportManifestSignature file(String file) => call(file: file);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportManifestSignature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportManifestSignature(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportManifestSignature call({
    Object? alg = const $CopyWithPlaceholder(),
    Object? kid = const $CopyWithPlaceholder(),
    Object? file = const $CopyWithPlaceholder(),
  }) {
    return ExportManifestSignature(
      alg: alg == const $CopyWithPlaceholder() || alg == null
          ? _value.alg
          // ignore: cast_nullable_to_non_nullable
          : alg as ExportManifestSignatureAlgEnum,
      kid: kid == const $CopyWithPlaceholder() || kid == null
          ? _value.kid
          // ignore: cast_nullable_to_non_nullable
          : kid as String,
      file: file == const $CopyWithPlaceholder() || file == null
          ? _value.file
          // ignore: cast_nullable_to_non_nullable
          : file as String,
    );
  }
}

extension $ExportManifestSignatureCopyWith on ExportManifestSignature {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportManifestSignature.copyWith(...)` or `instanceOfExportManifestSignature.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportManifestSignatureCWProxy get copyWith =>
      _$ExportManifestSignatureCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportManifestSignature _$ExportManifestSignatureFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportManifestSignature', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['alg', 'kid', 'file']);
  final val = ExportManifestSignature(
    alg: $checkedConvert(
      'alg',
      (v) => $enumDecode(_$ExportManifestSignatureAlgEnumEnumMap, v),
    ),
    kid: $checkedConvert('kid', (v) => v as String),
    file: $checkedConvert('file', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ExportManifestSignatureToJson(
  ExportManifestSignature instance,
) => <String, dynamic>{
  'alg': _$ExportManifestSignatureAlgEnumEnumMap[instance.alg]!,
  'kid': instance.kid,
  'file': instance.file,
};

const _$ExportManifestSignatureAlgEnumEnumMap = {
  ExportManifestSignatureAlgEnum.ed25519: 'Ed25519',
  ExportManifestSignatureAlgEnum.edDSA: 'EdDSA',
};
