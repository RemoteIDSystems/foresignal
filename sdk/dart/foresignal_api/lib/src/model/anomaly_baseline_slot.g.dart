// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_baseline_slot.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyBaselineSlotCWProxy {
  AnomalyBaselineSlot groupKey(String groupKey);

  AnomalyBaselineSlot slot(int slot);

  AnomalyBaselineSlot n(int n);

  AnomalyBaselineSlot mean(num mean);

  AnomalyBaselineSlot variance(num variance);

  AnomalyBaselineSlot model(AnomalyBaselineSlotModelEnum model);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyBaselineSlot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyBaselineSlot(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyBaselineSlot call({
    String groupKey,
    int slot,
    int n,
    num mean,
    num variance,
    AnomalyBaselineSlotModelEnum model,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyBaselineSlot.copyWith(...)` or call `instanceOfAnomalyBaselineSlot.copyWith.fieldName(value)` for a single field.
class _$AnomalyBaselineSlotCWProxyImpl implements _$AnomalyBaselineSlotCWProxy {
  const _$AnomalyBaselineSlotCWProxyImpl(this._value);

  final AnomalyBaselineSlot _value;

  @override
  AnomalyBaselineSlot groupKey(String groupKey) => call(groupKey: groupKey);

  @override
  AnomalyBaselineSlot slot(int slot) => call(slot: slot);

  @override
  AnomalyBaselineSlot n(int n) => call(n: n);

  @override
  AnomalyBaselineSlot mean(num mean) => call(mean: mean);

  @override
  AnomalyBaselineSlot variance(num variance) => call(variance: variance);

  @override
  AnomalyBaselineSlot model(AnomalyBaselineSlotModelEnum model) =>
      call(model: model);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyBaselineSlot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyBaselineSlot(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyBaselineSlot call({
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? slot = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? mean = const $CopyWithPlaceholder(),
    Object? variance = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
  }) {
    return AnomalyBaselineSlot(
      groupKey: groupKey == const $CopyWithPlaceholder() || groupKey == null
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String,
      slot: slot == const $CopyWithPlaceholder() || slot == null
          ? _value.slot
          // ignore: cast_nullable_to_non_nullable
          : slot as int,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      mean: mean == const $CopyWithPlaceholder() || mean == null
          ? _value.mean
          // ignore: cast_nullable_to_non_nullable
          : mean as num,
      variance: variance == const $CopyWithPlaceholder() || variance == null
          ? _value.variance
          // ignore: cast_nullable_to_non_nullable
          : variance as num,
      model: model == const $CopyWithPlaceholder() || model == null
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as AnomalyBaselineSlotModelEnum,
    );
  }
}

extension $AnomalyBaselineSlotCopyWith on AnomalyBaselineSlot {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyBaselineSlot.copyWith(...)` or `instanceOfAnomalyBaselineSlot.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyBaselineSlotCWProxy get copyWith =>
      _$AnomalyBaselineSlotCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyBaselineSlot _$AnomalyBaselineSlotFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnomalyBaselineSlot', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'groupKey',
          'slot',
          'n',
          'mean',
          'variance',
          'model',
        ],
      );
      final val = AnomalyBaselineSlot(
        groupKey: $checkedConvert('groupKey', (v) => v as String),
        slot: $checkedConvert('slot', (v) => (v as num).toInt()),
        n: $checkedConvert('n', (v) => (v as num).toInt()),
        mean: $checkedConvert('mean', (v) => v as num),
        variance: $checkedConvert('variance', (v) => v as num),
        model: $checkedConvert(
          'model',
          (v) => $enumDecode(_$AnomalyBaselineSlotModelEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnomalyBaselineSlotToJson(
  AnomalyBaselineSlot instance,
) => <String, dynamic>{
  'groupKey': instance.groupKey,
  'slot': instance.slot,
  'n': instance.n,
  'mean': instance.mean,
  'variance': instance.variance,
  'model': _$AnomalyBaselineSlotModelEnumEnumMap[instance.model]!,
};

const _$AnomalyBaselineSlotModelEnumEnumMap = {
  AnomalyBaselineSlotModelEnum.poisson: 'poisson',
  AnomalyBaselineSlotModelEnum.nb: 'nb',
};
