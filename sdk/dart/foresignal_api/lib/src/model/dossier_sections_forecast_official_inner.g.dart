// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_forecast_official_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsForecastOfficialInnerCWProxy {
  DossierSectionsForecastOfficialInner ref(String ref);

  DossierSectionsForecastOfficialInner title(String? title);

  DossierSectionsForecastOfficialInner kind(String? kind);

  DossierSectionsForecastOfficialInner issuer(String issuer);

  DossierSectionsForecastOfficialInner issuedAt(DateTime? issuedAt);

  DossierSectionsForecastOfficialInner validFrom(DateTime? validFrom);

  DossierSectionsForecastOfficialInner validTo(DateTime? validTo);

  DossierSectionsForecastOfficialInner note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsForecastOfficialInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsForecastOfficialInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsForecastOfficialInner call({
    String ref,
    String? title,
    String? kind,
    String issuer,
    DateTime? issuedAt,
    DateTime? validFrom,
    DateTime? validTo,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsForecastOfficialInner.copyWith(...)` or call `instanceOfDossierSectionsForecastOfficialInner.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsForecastOfficialInnerCWProxyImpl
    implements _$DossierSectionsForecastOfficialInnerCWProxy {
  const _$DossierSectionsForecastOfficialInnerCWProxyImpl(this._value);

  final DossierSectionsForecastOfficialInner _value;

  @override
  DossierSectionsForecastOfficialInner ref(String ref) => call(ref: ref);

  @override
  DossierSectionsForecastOfficialInner title(String? title) =>
      call(title: title);

  @override
  DossierSectionsForecastOfficialInner kind(String? kind) => call(kind: kind);

  @override
  DossierSectionsForecastOfficialInner issuer(String issuer) =>
      call(issuer: issuer);

  @override
  DossierSectionsForecastOfficialInner issuedAt(DateTime? issuedAt) =>
      call(issuedAt: issuedAt);

  @override
  DossierSectionsForecastOfficialInner validFrom(DateTime? validFrom) =>
      call(validFrom: validFrom);

  @override
  DossierSectionsForecastOfficialInner validTo(DateTime? validTo) =>
      call(validTo: validTo);

  @override
  DossierSectionsForecastOfficialInner note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsForecastOfficialInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsForecastOfficialInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsForecastOfficialInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? issuer = const $CopyWithPlaceholder(),
    Object? issuedAt = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsForecastOfficialInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      issuer: issuer == const $CopyWithPlaceholder() || issuer == null
          ? _value.issuer
          // ignore: cast_nullable_to_non_nullable
          : issuer as String,
      issuedAt: issuedAt == const $CopyWithPlaceholder()
          ? _value.issuedAt
          // ignore: cast_nullable_to_non_nullable
          : issuedAt as DateTime?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $DossierSectionsForecastOfficialInnerCopyWith
    on DossierSectionsForecastOfficialInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsForecastOfficialInner.copyWith(...)` or `instanceOfDossierSectionsForecastOfficialInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsForecastOfficialInnerCWProxy get copyWith =>
      _$DossierSectionsForecastOfficialInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsForecastOfficialInner
_$DossierSectionsForecastOfficialInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DossierSectionsForecastOfficialInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['ref', 'issuer']);
      final val = DossierSectionsForecastOfficialInner(
        ref: $checkedConvert('ref', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String?),
        kind: $checkedConvert('kind', (v) => v as String?),
        issuer: $checkedConvert('issuer', (v) => v as String? ?? 'official'),
        issuedAt: $checkedConvert(
          'issuedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validTo: $checkedConvert(
          'validTo',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$DossierSectionsForecastOfficialInnerToJson(
  DossierSectionsForecastOfficialInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'title': ?instance.title,
  'kind': ?instance.kind,
  'issuer': instance.issuer,
  'issuedAt': ?instance.issuedAt?.toIso8601String(),
  'validFrom': ?instance.validFrom?.toIso8601String(),
  'validTo': ?instance.validTo?.toIso8601String(),
  'note': ?instance.note,
};
