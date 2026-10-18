// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_evidence_corroboration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationEvidenceCorroborationCWProxy {
  SituationEvidenceCorroboration corroborated(int corroborated);

  SituationEvidenceCorroboration uncorroborated(int uncorroborated);

  SituationEvidenceCorroboration unknown(int? unknown);

  SituationEvidenceCorroboration notApplicable(int? notApplicable);

  SituationEvidenceCorroboration share(num? share);

  SituationEvidenceCorroboration uncorroboratedOnly(bool? uncorroboratedOnly);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidenceCorroboration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidenceCorroboration(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationEvidenceCorroboration call({
    int corroborated,
    int uncorroborated,
    int? unknown,
    int? notApplicable,
    num? share,
    bool? uncorroboratedOnly,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationEvidenceCorroboration.copyWith(...)` or call `instanceOfSituationEvidenceCorroboration.copyWith.fieldName(value)` for a single field.
class _$SituationEvidenceCorroborationCWProxyImpl
    implements _$SituationEvidenceCorroborationCWProxy {
  const _$SituationEvidenceCorroborationCWProxyImpl(this._value);

  final SituationEvidenceCorroboration _value;

  @override
  SituationEvidenceCorroboration corroborated(int corroborated) =>
      call(corroborated: corroborated);

  @override
  SituationEvidenceCorroboration uncorroborated(int uncorroborated) =>
      call(uncorroborated: uncorroborated);

  @override
  SituationEvidenceCorroboration unknown(int? unknown) =>
      call(unknown: unknown);

  @override
  SituationEvidenceCorroboration notApplicable(int? notApplicable) =>
      call(notApplicable: notApplicable);

  @override
  SituationEvidenceCorroboration share(num? share) => call(share: share);

  @override
  SituationEvidenceCorroboration uncorroboratedOnly(bool? uncorroboratedOnly) =>
      call(uncorroboratedOnly: uncorroboratedOnly);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidenceCorroboration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidenceCorroboration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationEvidenceCorroboration call({
    Object? corroborated = const $CopyWithPlaceholder(),
    Object? uncorroborated = const $CopyWithPlaceholder(),
    Object? unknown = const $CopyWithPlaceholder(),
    Object? notApplicable = const $CopyWithPlaceholder(),
    Object? share = const $CopyWithPlaceholder(),
    Object? uncorroboratedOnly = const $CopyWithPlaceholder(),
  }) {
    return SituationEvidenceCorroboration(
      corroborated:
          corroborated == const $CopyWithPlaceholder() || corroborated == null
          ? _value.corroborated
          // ignore: cast_nullable_to_non_nullable
          : corroborated as int,
      uncorroborated:
          uncorroborated == const $CopyWithPlaceholder() ||
              uncorroborated == null
          ? _value.uncorroborated
          // ignore: cast_nullable_to_non_nullable
          : uncorroborated as int,
      unknown: unknown == const $CopyWithPlaceholder()
          ? _value.unknown
          // ignore: cast_nullable_to_non_nullable
          : unknown as int?,
      notApplicable: notApplicable == const $CopyWithPlaceholder()
          ? _value.notApplicable
          // ignore: cast_nullable_to_non_nullable
          : notApplicable as int?,
      share: share == const $CopyWithPlaceholder()
          ? _value.share
          // ignore: cast_nullable_to_non_nullable
          : share as num?,
      uncorroboratedOnly: uncorroboratedOnly == const $CopyWithPlaceholder()
          ? _value.uncorroboratedOnly
          // ignore: cast_nullable_to_non_nullable
          : uncorroboratedOnly as bool?,
    );
  }
}

extension $SituationEvidenceCorroborationCopyWith
    on SituationEvidenceCorroboration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationEvidenceCorroboration.copyWith(...)` or `instanceOfSituationEvidenceCorroboration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationEvidenceCorroborationCWProxy get copyWith =>
      _$SituationEvidenceCorroborationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationEvidenceCorroboration _$SituationEvidenceCorroborationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationEvidenceCorroboration', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['corroborated', 'uncorroborated', 'share'],
  );
  final val = SituationEvidenceCorroboration(
    corroborated: $checkedConvert('corroborated', (v) => (v as num).toInt()),
    uncorroborated: $checkedConvert(
      'uncorroborated',
      (v) => (v as num).toInt(),
    ),
    unknown: $checkedConvert('unknown', (v) => (v as num?)?.toInt()),
    notApplicable: $checkedConvert(
      'notApplicable',
      (v) => (v as num?)?.toInt(),
    ),
    share: $checkedConvert('share', (v) => v as num?),
    uncorroboratedOnly: $checkedConvert(
      'uncorroboratedOnly',
      (v) => v as bool?,
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationEvidenceCorroborationToJson(
  SituationEvidenceCorroboration instance,
) => <String, dynamic>{
  'corroborated': instance.corroborated,
  'uncorroborated': instance.uncorroborated,
  'unknown': ?instance.unknown,
  'notApplicable': ?instance.notApplicable,
  'share': instance.share,
  'uncorroboratedOnly': ?instance.uncorroboratedOnly,
};
