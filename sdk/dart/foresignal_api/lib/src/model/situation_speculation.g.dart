// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_speculation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationSpeculationCWProxy {
  SituationSpeculation label(SituationSpeculationLabelEnum label);

  SituationSpeculation textOrigin(
    SituationSpeculationTextOriginEnum? textOrigin,
  );

  SituationSpeculation brief(String? brief);

  SituationSpeculation briefGeneratedAt(DateTime? briefGeneratedAt);

  SituationSpeculation briefModel(String? briefModel);

  SituationSpeculation manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSpeculation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSpeculation(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationSpeculation call({
    SituationSpeculationLabelEnum label,
    SituationSpeculationTextOriginEnum? textOrigin,
    String? brief,
    DateTime? briefGeneratedAt,
    String? briefModel,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationSpeculation.copyWith(...)` or call `instanceOfSituationSpeculation.copyWith.fieldName(value)` for a single field.
class _$SituationSpeculationCWProxyImpl
    implements _$SituationSpeculationCWProxy {
  const _$SituationSpeculationCWProxyImpl(this._value);

  final SituationSpeculation _value;

  @override
  SituationSpeculation label(SituationSpeculationLabelEnum label) =>
      call(label: label);

  @override
  SituationSpeculation textOrigin(
    SituationSpeculationTextOriginEnum? textOrigin,
  ) => call(textOrigin: textOrigin);

  @override
  SituationSpeculation brief(String? brief) => call(brief: brief);

  @override
  SituationSpeculation briefGeneratedAt(DateTime? briefGeneratedAt) =>
      call(briefGeneratedAt: briefGeneratedAt);

  @override
  SituationSpeculation briefModel(String? briefModel) =>
      call(briefModel: briefModel);

  @override
  SituationSpeculation manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSpeculation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSpeculation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationSpeculation call({
    Object? label = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? brief = const $CopyWithPlaceholder(),
    Object? briefGeneratedAt = const $CopyWithPlaceholder(),
    Object? briefModel = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return SituationSpeculation(
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as SituationSpeculationLabelEnum,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as SituationSpeculationTextOriginEnum?,
      brief: brief == const $CopyWithPlaceholder()
          ? _value.brief
          // ignore: cast_nullable_to_non_nullable
          : brief as String?,
      briefGeneratedAt: briefGeneratedAt == const $CopyWithPlaceholder()
          ? _value.briefGeneratedAt
          // ignore: cast_nullable_to_non_nullable
          : briefGeneratedAt as DateTime?,
      briefModel: briefModel == const $CopyWithPlaceholder()
          ? _value.briefModel
          // ignore: cast_nullable_to_non_nullable
          : briefModel as String?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $SituationSpeculationCopyWith on SituationSpeculation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationSpeculation.copyWith(...)` or `instanceOfSituationSpeculation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationSpeculationCWProxy get copyWith =>
      _$SituationSpeculationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationSpeculation _$SituationSpeculationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationSpeculation', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['label', 'brief']);
  final val = SituationSpeculation(
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$SituationSpeculationLabelEnumEnumMap, v),
    ),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) =>
          $enumDecodeNullable(_$SituationSpeculationTextOriginEnumEnumMap, v),
    ),
    brief: $checkedConvert('brief', (v) => v as String?),
    briefGeneratedAt: $checkedConvert(
      'briefGeneratedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    briefModel: $checkedConvert('briefModel', (v) => v as String?),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SituationSpeculationToJson(
  SituationSpeculation instance,
) => <String, dynamic>{
  'label': _$SituationSpeculationLabelEnumEnumMap[instance.label]!,
  'textOrigin':
      ?_$SituationSpeculationTextOriginEnumEnumMap[instance.textOrigin],
  'brief': instance.brief,
  'briefGeneratedAt': ?instance.briefGeneratedAt?.toIso8601String(),
  'briefModel': ?instance.briefModel,
  'manifestId': ?instance.manifestId,
};

const _$SituationSpeculationLabelEnumEnumMap = {
  SituationSpeculationLabelEnum.speculation: 'speculation',
};

const _$SituationSpeculationTextOriginEnumEnumMap = {
  SituationSpeculationTextOriginEnum.llm: 'llm',
};
