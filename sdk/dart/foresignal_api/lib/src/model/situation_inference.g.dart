// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_inference.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationInferenceCWProxy {
  SituationInference summary(String summary);

  SituationInference summaryTemplateVersion(String? summaryTemplateVersion);

  SituationInference statusBasis(String statusBasis);

  SituationInference severityBasis(String severityBasis);

  SituationInference linker(DetectorRef? linker);

  SituationInference manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationInference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationInference(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationInference call({
    String summary,
    String? summaryTemplateVersion,
    String statusBasis,
    String severityBasis,
    DetectorRef? linker,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationInference.copyWith(...)` or call `instanceOfSituationInference.copyWith.fieldName(value)` for a single field.
class _$SituationInferenceCWProxyImpl implements _$SituationInferenceCWProxy {
  const _$SituationInferenceCWProxyImpl(this._value);

  final SituationInference _value;

  @override
  SituationInference summary(String summary) => call(summary: summary);

  @override
  SituationInference summaryTemplateVersion(String? summaryTemplateVersion) =>
      call(summaryTemplateVersion: summaryTemplateVersion);

  @override
  SituationInference statusBasis(String statusBasis) =>
      call(statusBasis: statusBasis);

  @override
  SituationInference severityBasis(String severityBasis) =>
      call(severityBasis: severityBasis);

  @override
  SituationInference linker(DetectorRef? linker) => call(linker: linker);

  @override
  SituationInference manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationInference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationInference(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationInference call({
    Object? summary = const $CopyWithPlaceholder(),
    Object? summaryTemplateVersion = const $CopyWithPlaceholder(),
    Object? statusBasis = const $CopyWithPlaceholder(),
    Object? severityBasis = const $CopyWithPlaceholder(),
    Object? linker = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return SituationInference(
      summary: summary == const $CopyWithPlaceholder() || summary == null
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String,
      summaryTemplateVersion:
          summaryTemplateVersion == const $CopyWithPlaceholder()
          ? _value.summaryTemplateVersion
          // ignore: cast_nullable_to_non_nullable
          : summaryTemplateVersion as String?,
      statusBasis:
          statusBasis == const $CopyWithPlaceholder() || statusBasis == null
          ? _value.statusBasis
          // ignore: cast_nullable_to_non_nullable
          : statusBasis as String,
      severityBasis:
          severityBasis == const $CopyWithPlaceholder() || severityBasis == null
          ? _value.severityBasis
          // ignore: cast_nullable_to_non_nullable
          : severityBasis as String,
      linker: linker == const $CopyWithPlaceholder()
          ? _value.linker
          // ignore: cast_nullable_to_non_nullable
          : linker as DetectorRef?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $SituationInferenceCopyWith on SituationInference {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationInference.copyWith(...)` or `instanceOfSituationInference.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationInferenceCWProxy get copyWith =>
      _$SituationInferenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationInference _$SituationInferenceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationInference', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['summary', 'statusBasis', 'severityBasis'],
      );
      final val = SituationInference(
        summary: $checkedConvert('summary', (v) => v as String),
        summaryTemplateVersion: $checkedConvert(
          'summaryTemplateVersion',
          (v) => v as String?,
        ),
        statusBasis: $checkedConvert('statusBasis', (v) => v as String),
        severityBasis: $checkedConvert('severityBasis', (v) => v as String),
        linker: $checkedConvert(
          'linker',
          (v) => v == null
              ? null
              : DetectorRef.fromJson(v as Map<String, dynamic>),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SituationInferenceToJson(SituationInference instance) =>
    <String, dynamic>{
      'summary': instance.summary,
      'summaryTemplateVersion': ?instance.summaryTemplateVersion,
      'statusBasis': instance.statusBasis,
      'severityBasis': instance.severityBasis,
      'linker': ?instance.linker?.toJson(),
      'manifestId': ?instance.manifestId,
    };
