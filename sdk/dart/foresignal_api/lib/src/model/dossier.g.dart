// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierCWProxy {
  Dossier ref(String ref);

  Dossier envelope(ObjectEnvelope envelope);

  Dossier asOf(DateTime? asOf);

  Dossier generatedAt(DateTime generatedAt);

  Dossier sections(DossierSections sections);

  Dossier truncated(bool? truncated);

  Dossier coverage(DossierCoverage? coverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Dossier(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Dossier(...).copyWith(id: 12, name: "My name")
  /// ```
  Dossier call({
    String ref,
    ObjectEnvelope envelope,
    DateTime? asOf,
    DateTime generatedAt,
    DossierSections sections,
    bool? truncated,
    DossierCoverage? coverage,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossier.copyWith(...)` or call `instanceOfDossier.copyWith.fieldName(value)` for a single field.
class _$DossierCWProxyImpl implements _$DossierCWProxy {
  const _$DossierCWProxyImpl(this._value);

  final Dossier _value;

  @override
  Dossier ref(String ref) => call(ref: ref);

  @override
  Dossier envelope(ObjectEnvelope envelope) => call(envelope: envelope);

  @override
  Dossier asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  Dossier generatedAt(DateTime generatedAt) => call(generatedAt: generatedAt);

  @override
  Dossier sections(DossierSections sections) => call(sections: sections);

  @override
  Dossier truncated(bool? truncated) => call(truncated: truncated);

  @override
  Dossier coverage(DossierCoverage? coverage) => call(coverage: coverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Dossier(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Dossier(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Dossier call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? sections = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
  }) {
    return Dossier(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      envelope: envelope == const $CopyWithPlaceholder() || envelope == null
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      sections: sections == const $CopyWithPlaceholder() || sections == null
          ? _value.sections
          // ignore: cast_nullable_to_non_nullable
          : sections as DossierSections,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as DossierCoverage?,
    );
  }
}

extension $DossierCopyWith on Dossier {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossier.copyWith(...)` or `instanceOfDossier.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierCWProxy get copyWith => _$DossierCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Dossier _$DossierFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Dossier', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['ref', 'envelope', 'generatedAt', 'sections'],
      );
      final val = Dossier(
        ref: $checkedConvert('ref', (v) => v as String),
        envelope: $checkedConvert(
          'envelope',
          (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => DateTime.parse(v as String),
        ),
        sections: $checkedConvert(
          'sections',
          (v) => DossierSections.fromJson(v as Map<String, dynamic>),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
        coverage: $checkedConvert(
          'coverage',
          (v) => v == null
              ? null
              : DossierCoverage.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DossierToJson(Dossier instance) => <String, dynamic>{
  'ref': instance.ref,
  'envelope': instance.envelope.toJson(),
  'asOf': ?instance.asOf?.toIso8601String(),
  'generatedAt': instance.generatedAt.toIso8601String(),
  'sections': instance.sections.toJson(),
  'truncated': ?instance.truncated,
  'coverage': ?instance.coverage?.toJson(),
};
