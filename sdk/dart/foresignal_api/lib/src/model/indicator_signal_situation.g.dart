// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_signal_situation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorSignalSituationCWProxy {
  IndicatorSignalSituation situationId(String situationId);

  IndicatorSignalSituation status(IndicatorSignalSituationStatusEnum? status);

  IndicatorSignalSituation minSeverity(Severity? minSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalSituation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalSituation(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorSignalSituation call({
    String situationId,
    IndicatorSignalSituationStatusEnum? status,
    Severity? minSeverity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorSignalSituation.copyWith(...)` or call `instanceOfIndicatorSignalSituation.copyWith.fieldName(value)` for a single field.
class _$IndicatorSignalSituationCWProxyImpl
    implements _$IndicatorSignalSituationCWProxy {
  const _$IndicatorSignalSituationCWProxyImpl(this._value);

  final IndicatorSignalSituation _value;

  @override
  IndicatorSignalSituation situationId(String situationId) =>
      call(situationId: situationId);

  @override
  IndicatorSignalSituation status(IndicatorSignalSituationStatusEnum? status) =>
      call(status: status);

  @override
  IndicatorSignalSituation minSeverity(Severity? minSeverity) =>
      call(minSeverity: minSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalSituation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalSituation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorSignalSituation call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
  }) {
    return IndicatorSignalSituation(
      situationId:
          situationId == const $CopyWithPlaceholder() || situationId == null
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IndicatorSignalSituationStatusEnum?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as Severity?,
    );
  }
}

extension $IndicatorSignalSituationCopyWith on IndicatorSignalSituation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorSignalSituation.copyWith(...)` or `instanceOfIndicatorSignalSituation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorSignalSituationCWProxy get copyWith =>
      _$IndicatorSignalSituationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorSignalSituation _$IndicatorSignalSituationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndicatorSignalSituation', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['situationId']);
  final val = IndicatorSignalSituation(
    situationId: $checkedConvert('situationId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) =>
          $enumDecodeNullable(_$IndicatorSignalSituationStatusEnumEnumMap, v),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$IndicatorSignalSituationToJson(
  IndicatorSignalSituation instance,
) => <String, dynamic>{
  'situationId': instance.situationId,
  'status': ?_$IndicatorSignalSituationStatusEnumEnumMap[instance.status],
  'minSeverity': ?_$SeverityEnumMap[instance.minSeverity],
};

const _$IndicatorSignalSituationStatusEnumEnumMap = {
  IndicatorSignalSituationStatusEnum.emerging: 'emerging',
  IndicatorSignalSituationStatusEnum.active: 'active',
  IndicatorSignalSituationStatusEnum.dormant: 'dormant',
  IndicatorSignalSituationStatusEnum.closed: 'closed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
