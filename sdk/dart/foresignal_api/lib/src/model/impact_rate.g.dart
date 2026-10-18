// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'impact_rate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ImpactRateCWProxy {
  ImpactRate hazardClass(String hazardClass);

  ImpactRate impactFamily(String impactFamily);

  ImpactRate regionClass(String regionClass);

  ImpactRate lagHours(int? lagHours);

  ImpactRate rate(num? rate);

  ImpactRate interval(ImpactRateInterval? interval);

  ImpactRate n(int n);

  ImpactRate nImpacts(int? nImpacts);

  ImpactRate source_(ImpactRateSource_Enum? source_);

  ImpactRate status(ImpactRateStatusEnum status);

  ImpactRate baseRateId(String? baseRateId);

  ImpactRate computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactRate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactRate(...).copyWith(id: 12, name: "My name")
  /// ```
  ImpactRate call({
    String hazardClass,
    String impactFamily,
    String regionClass,
    int? lagHours,
    num? rate,
    ImpactRateInterval? interval,
    int n,
    int? nImpacts,
    ImpactRateSource_Enum? source_,
    ImpactRateStatusEnum status,
    String? baseRateId,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfImpactRate.copyWith(...)` or call `instanceOfImpactRate.copyWith.fieldName(value)` for a single field.
class _$ImpactRateCWProxyImpl implements _$ImpactRateCWProxy {
  const _$ImpactRateCWProxyImpl(this._value);

  final ImpactRate _value;

  @override
  ImpactRate hazardClass(String hazardClass) => call(hazardClass: hazardClass);

  @override
  ImpactRate impactFamily(String impactFamily) =>
      call(impactFamily: impactFamily);

  @override
  ImpactRate regionClass(String regionClass) => call(regionClass: regionClass);

  @override
  ImpactRate lagHours(int? lagHours) => call(lagHours: lagHours);

  @override
  ImpactRate rate(num? rate) => call(rate: rate);

  @override
  ImpactRate interval(ImpactRateInterval? interval) => call(interval: interval);

  @override
  ImpactRate n(int n) => call(n: n);

  @override
  ImpactRate nImpacts(int? nImpacts) => call(nImpacts: nImpacts);

  @override
  ImpactRate source_(ImpactRateSource_Enum? source_) => call(source_: source_);

  @override
  ImpactRate status(ImpactRateStatusEnum status) => call(status: status);

  @override
  ImpactRate baseRateId(String? baseRateId) => call(baseRateId: baseRateId);

  @override
  ImpactRate computedAt(DateTime computedAt) => call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactRate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactRate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ImpactRate call({
    Object? hazardClass = const $CopyWithPlaceholder(),
    Object? impactFamily = const $CopyWithPlaceholder(),
    Object? regionClass = const $CopyWithPlaceholder(),
    Object? lagHours = const $CopyWithPlaceholder(),
    Object? rate = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? nImpacts = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? baseRateId = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return ImpactRate(
      hazardClass:
          hazardClass == const $CopyWithPlaceholder() || hazardClass == null
          ? _value.hazardClass
          // ignore: cast_nullable_to_non_nullable
          : hazardClass as String,
      impactFamily:
          impactFamily == const $CopyWithPlaceholder() || impactFamily == null
          ? _value.impactFamily
          // ignore: cast_nullable_to_non_nullable
          : impactFamily as String,
      regionClass:
          regionClass == const $CopyWithPlaceholder() || regionClass == null
          ? _value.regionClass
          // ignore: cast_nullable_to_non_nullable
          : regionClass as String,
      lagHours: lagHours == const $CopyWithPlaceholder()
          ? _value.lagHours
          // ignore: cast_nullable_to_non_nullable
          : lagHours as int?,
      rate: rate == const $CopyWithPlaceholder()
          ? _value.rate
          // ignore: cast_nullable_to_non_nullable
          : rate as num?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as ImpactRateInterval?,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      nImpacts: nImpacts == const $CopyWithPlaceholder()
          ? _value.nImpacts
          // ignore: cast_nullable_to_non_nullable
          : nImpacts as int?,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as ImpactRateSource_Enum?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ImpactRateStatusEnum,
      baseRateId: baseRateId == const $CopyWithPlaceholder()
          ? _value.baseRateId
          // ignore: cast_nullable_to_non_nullable
          : baseRateId as String?,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
    );
  }
}

extension $ImpactRateCopyWith on ImpactRate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfImpactRate.copyWith(...)` or `instanceOfImpactRate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ImpactRateCWProxy get copyWith => _$ImpactRateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ImpactRate _$ImpactRateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ImpactRate', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'hazardClass',
          'impactFamily',
          'regionClass',
          'n',
          'status',
          'computedAt',
        ],
      );
      final val = ImpactRate(
        hazardClass: $checkedConvert('hazardClass', (v) => v as String),
        impactFamily: $checkedConvert('impactFamily', (v) => v as String),
        regionClass: $checkedConvert('regionClass', (v) => v as String),
        lagHours: $checkedConvert('lagHours', (v) => (v as num?)?.toInt()),
        rate: $checkedConvert('rate', (v) => v as num?),
        interval: $checkedConvert(
          'interval',
          (v) => v == null
              ? null
              : ImpactRateInterval.fromJson(v as Map<String, dynamic>),
        ),
        n: $checkedConvert('n', (v) => (v as num).toInt()),
        nImpacts: $checkedConvert('nImpacts', (v) => (v as num?)?.toInt()),
        source_: $checkedConvert(
          'source',
          (v) => $enumDecodeNullable(_$ImpactRateSource_EnumEnumMap, v),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ImpactRateStatusEnumEnumMap, v),
        ),
        baseRateId: $checkedConvert('baseRateId', (v) => v as String?),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$ImpactRateToJson(ImpactRate instance) =>
    <String, dynamic>{
      'hazardClass': instance.hazardClass,
      'impactFamily': instance.impactFamily,
      'regionClass': instance.regionClass,
      'lagHours': ?instance.lagHours,
      'rate': ?instance.rate,
      'interval': ?instance.interval?.toJson(),
      'n': instance.n,
      'nImpacts': ?instance.nImpacts,
      'source': ?_$ImpactRateSource_EnumEnumMap[instance.source_],
      'status': _$ImpactRateStatusEnumEnumMap[instance.status]!,
      'baseRateId': ?instance.baseRateId,
      'computedAt': instance.computedAt.toIso8601String(),
    };

const _$ImpactRateSource_EnumEnumMap = {
  ImpactRateSource_Enum.ownHistory: 'own-history',
  ImpactRateSource_Enum.referenceImport: 'reference-import',
  ImpactRateSource_Enum.mixed: 'mixed',
};

const _$ImpactRateStatusEnumEnumMap = {
  ImpactRateStatusEnum.ok: 'ok',
  ImpactRateStatusEnum.insufficient: 'insufficient',
};
