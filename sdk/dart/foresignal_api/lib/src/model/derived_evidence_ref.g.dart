// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'derived_evidence_ref.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DerivedEvidenceRefCWProxy {
  DerivedEvidenceRef sourceId(String sourceId);

  DerivedEvidenceRef externalId(String externalId);

  DerivedEvidenceRef observedAt(DateTime observedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DerivedEvidenceRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DerivedEvidenceRef(...).copyWith(id: 12, name: "My name")
  /// ```
  DerivedEvidenceRef call({
    String sourceId,
    String externalId,
    DateTime observedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDerivedEvidenceRef.copyWith(...)` or call `instanceOfDerivedEvidenceRef.copyWith.fieldName(value)` for a single field.
class _$DerivedEvidenceRefCWProxyImpl implements _$DerivedEvidenceRefCWProxy {
  const _$DerivedEvidenceRefCWProxyImpl(this._value);

  final DerivedEvidenceRef _value;

  @override
  DerivedEvidenceRef sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  DerivedEvidenceRef externalId(String externalId) =>
      call(externalId: externalId);

  @override
  DerivedEvidenceRef observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DerivedEvidenceRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DerivedEvidenceRef(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DerivedEvidenceRef call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
  }) {
    return DerivedEvidenceRef(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
    );
  }
}

extension $DerivedEvidenceRefCopyWith on DerivedEvidenceRef {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDerivedEvidenceRef.copyWith(...)` or `instanceOfDerivedEvidenceRef.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DerivedEvidenceRefCWProxy get copyWith =>
      _$DerivedEvidenceRefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DerivedEvidenceRef _$DerivedEvidenceRefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DerivedEvidenceRef', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['sourceId', 'externalId', 'observedAt'],
      );
      final val = DerivedEvidenceRef(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DerivedEvidenceRefToJson(DerivedEvidenceRef instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'observedAt': instance.observedAt.toIso8601String(),
    };
