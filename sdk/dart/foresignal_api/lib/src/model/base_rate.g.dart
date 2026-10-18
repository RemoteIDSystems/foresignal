// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateCWProxy {
  BaseRate id(String? id);

  BaseRate name(String? name);

  BaseRate definition(BaseRateDefinition definition);

  BaseRate referenceClass(String referenceClass);

  BaseRate ratePerWindow(num ratePerWindow);

  BaseRate pAtLeastOne(num pAtLeastOne);

  BaseRate interval(StatInterval interval);

  BaseRate nWindows(int nWindows);

  BaseRate nEvents(int nEvents);

  BaseRate periodFrom(DateTime periodFrom);

  BaseRate periodTo(DateTime periodTo);

  BaseRate sourceMix(Map<String, int> sourceMix);

  BaseRate licences(List<BaseRateLicencesInner>? licences);

  BaseRate unconditional(BaseRateUnconditional? unconditional);

  BaseRate regionProfiles(List<RegionProfile>? regionProfiles);

  BaseRate caveats(List<Caveat> caveats);

  BaseRate computer(DetectorRef computer);

  BaseRate computedAt(DateTime computedAt);

  BaseRate origin(BaseRateOriginEnum origin);

  BaseRate cached(bool? cached);

  BaseRate manifestId(String manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRate(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRate call({
    String? id,
    String? name,
    BaseRateDefinition definition,
    String referenceClass,
    num ratePerWindow,
    num pAtLeastOne,
    StatInterval interval,
    int nWindows,
    int nEvents,
    DateTime periodFrom,
    DateTime periodTo,
    Map<String, int> sourceMix,
    List<BaseRateLicencesInner>? licences,
    BaseRateUnconditional? unconditional,
    List<RegionProfile>? regionProfiles,
    List<Caveat> caveats,
    DetectorRef computer,
    DateTime computedAt,
    BaseRateOriginEnum origin,
    bool? cached,
    String manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRate.copyWith(...)` or call `instanceOfBaseRate.copyWith.fieldName(value)` for a single field.
class _$BaseRateCWProxyImpl implements _$BaseRateCWProxy {
  const _$BaseRateCWProxyImpl(this._value);

  final BaseRate _value;

  @override
  BaseRate id(String? id) => call(id: id);

  @override
  BaseRate name(String? name) => call(name: name);

  @override
  BaseRate definition(BaseRateDefinition definition) =>
      call(definition: definition);

  @override
  BaseRate referenceClass(String referenceClass) =>
      call(referenceClass: referenceClass);

  @override
  BaseRate ratePerWindow(num ratePerWindow) =>
      call(ratePerWindow: ratePerWindow);

  @override
  BaseRate pAtLeastOne(num pAtLeastOne) => call(pAtLeastOne: pAtLeastOne);

  @override
  BaseRate interval(StatInterval interval) => call(interval: interval);

  @override
  BaseRate nWindows(int nWindows) => call(nWindows: nWindows);

  @override
  BaseRate nEvents(int nEvents) => call(nEvents: nEvents);

  @override
  BaseRate periodFrom(DateTime periodFrom) => call(periodFrom: periodFrom);

  @override
  BaseRate periodTo(DateTime periodTo) => call(periodTo: periodTo);

  @override
  BaseRate sourceMix(Map<String, int> sourceMix) => call(sourceMix: sourceMix);

  @override
  BaseRate licences(List<BaseRateLicencesInner>? licences) =>
      call(licences: licences);

  @override
  BaseRate unconditional(BaseRateUnconditional? unconditional) =>
      call(unconditional: unconditional);

  @override
  BaseRate regionProfiles(List<RegionProfile>? regionProfiles) =>
      call(regionProfiles: regionProfiles);

  @override
  BaseRate caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  BaseRate computer(DetectorRef computer) => call(computer: computer);

  @override
  BaseRate computedAt(DateTime computedAt) => call(computedAt: computedAt);

  @override
  BaseRate origin(BaseRateOriginEnum origin) => call(origin: origin);

  @override
  BaseRate cached(bool? cached) => call(cached: cached);

  @override
  BaseRate manifestId(String manifestId) => call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRate call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
    Object? referenceClass = const $CopyWithPlaceholder(),
    Object? ratePerWindow = const $CopyWithPlaceholder(),
    Object? pAtLeastOne = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? nWindows = const $CopyWithPlaceholder(),
    Object? nEvents = const $CopyWithPlaceholder(),
    Object? periodFrom = const $CopyWithPlaceholder(),
    Object? periodTo = const $CopyWithPlaceholder(),
    Object? sourceMix = const $CopyWithPlaceholder(),
    Object? licences = const $CopyWithPlaceholder(),
    Object? unconditional = const $CopyWithPlaceholder(),
    Object? regionProfiles = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? computer = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? cached = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return BaseRate(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      definition:
          definition == const $CopyWithPlaceholder() || definition == null
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as BaseRateDefinition,
      referenceClass:
          referenceClass == const $CopyWithPlaceholder() ||
              referenceClass == null
          ? _value.referenceClass
          // ignore: cast_nullable_to_non_nullable
          : referenceClass as String,
      ratePerWindow:
          ratePerWindow == const $CopyWithPlaceholder() || ratePerWindow == null
          ? _value.ratePerWindow
          // ignore: cast_nullable_to_non_nullable
          : ratePerWindow as num,
      pAtLeastOne:
          pAtLeastOne == const $CopyWithPlaceholder() || pAtLeastOne == null
          ? _value.pAtLeastOne
          // ignore: cast_nullable_to_non_nullable
          : pAtLeastOne as num,
      interval: interval == const $CopyWithPlaceholder() || interval == null
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as StatInterval,
      nWindows: nWindows == const $CopyWithPlaceholder() || nWindows == null
          ? _value.nWindows
          // ignore: cast_nullable_to_non_nullable
          : nWindows as int,
      nEvents: nEvents == const $CopyWithPlaceholder() || nEvents == null
          ? _value.nEvents
          // ignore: cast_nullable_to_non_nullable
          : nEvents as int,
      periodFrom:
          periodFrom == const $CopyWithPlaceholder() || periodFrom == null
          ? _value.periodFrom
          // ignore: cast_nullable_to_non_nullable
          : periodFrom as DateTime,
      periodTo: periodTo == const $CopyWithPlaceholder() || periodTo == null
          ? _value.periodTo
          // ignore: cast_nullable_to_non_nullable
          : periodTo as DateTime,
      sourceMix: sourceMix == const $CopyWithPlaceholder() || sourceMix == null
          ? _value.sourceMix
          // ignore: cast_nullable_to_non_nullable
          : sourceMix as Map<String, int>,
      licences: licences == const $CopyWithPlaceholder()
          ? _value.licences
          // ignore: cast_nullable_to_non_nullable
          : licences as List<BaseRateLicencesInner>?,
      unconditional: unconditional == const $CopyWithPlaceholder()
          ? _value.unconditional
          // ignore: cast_nullable_to_non_nullable
          : unconditional as BaseRateUnconditional?,
      regionProfiles: regionProfiles == const $CopyWithPlaceholder()
          ? _value.regionProfiles
          // ignore: cast_nullable_to_non_nullable
          : regionProfiles as List<RegionProfile>?,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
      computer: computer == const $CopyWithPlaceholder() || computer == null
          ? _value.computer
          // ignore: cast_nullable_to_non_nullable
          : computer as DetectorRef,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as BaseRateOriginEnum,
      cached: cached == const $CopyWithPlaceholder()
          ? _value.cached
          // ignore: cast_nullable_to_non_nullable
          : cached as bool?,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
    );
  }
}

extension $BaseRateCopyWith on BaseRate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRate.copyWith(...)` or `instanceOfBaseRate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateCWProxy get copyWith => _$BaseRateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRate _$BaseRateFromJson(Map<String, dynamic> json) => $checkedCreate(
  'BaseRate',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'id',
        'definition',
        'referenceClass',
        'ratePerWindow',
        'pAtLeastOne',
        'interval',
        'nWindows',
        'nEvents',
        'periodFrom',
        'periodTo',
        'sourceMix',
        'caveats',
        'computer',
        'computedAt',
        'origin',
        'manifestId',
      ],
    );
    final val = BaseRate(
      id: $checkedConvert('id', (v) => v as String?),
      name: $checkedConvert('name', (v) => v as String?),
      definition: $checkedConvert(
        'definition',
        (v) => BaseRateDefinition.fromJson(v as Map<String, dynamic>),
      ),
      referenceClass: $checkedConvert('referenceClass', (v) => v as String),
      ratePerWindow: $checkedConvert('ratePerWindow', (v) => v as num),
      pAtLeastOne: $checkedConvert('pAtLeastOne', (v) => v as num),
      interval: $checkedConvert(
        'interval',
        (v) => StatInterval.fromJson(v as Map<String, dynamic>),
      ),
      nWindows: $checkedConvert('nWindows', (v) => (v as num).toInt()),
      nEvents: $checkedConvert('nEvents', (v) => (v as num).toInt()),
      periodFrom: $checkedConvert(
        'periodFrom',
        (v) => DateTime.parse(v as String),
      ),
      periodTo: $checkedConvert('periodTo', (v) => DateTime.parse(v as String)),
      sourceMix: $checkedConvert(
        'sourceMix',
        (v) => Map<String, int>.from(v as Map),
      ),
      licences: $checkedConvert(
        'licences',
        (v) => (v as List<dynamic>?)
            ?.map(
              (e) => BaseRateLicencesInner.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
      unconditional: $checkedConvert(
        'unconditional',
        (v) => v == null
            ? null
            : BaseRateUnconditional.fromJson(v as Map<String, dynamic>),
      ),
      regionProfiles: $checkedConvert(
        'regionProfiles',
        (v) => (v as List<dynamic>?)
            ?.map((e) => RegionProfile.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      caveats: $checkedConvert(
        'caveats',
        (v) => (v as List<dynamic>)
            .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      computer: $checkedConvert(
        'computer',
        (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
      ),
      computedAt: $checkedConvert(
        'computedAt',
        (v) => DateTime.parse(v as String),
      ),
      origin: $checkedConvert(
        'origin',
        (v) => $enumDecode(_$BaseRateOriginEnumEnumMap, v),
      ),
      cached: $checkedConvert('cached', (v) => v as bool?),
      manifestId: $checkedConvert('manifestId', (v) => v as String),
    );
    return val;
  },
);

Map<String, dynamic> _$BaseRateToJson(BaseRate instance) => <String, dynamic>{
  'id': instance.id,
  'name': ?instance.name,
  'definition': instance.definition.toJson(),
  'referenceClass': instance.referenceClass,
  'ratePerWindow': instance.ratePerWindow,
  'pAtLeastOne': instance.pAtLeastOne,
  'interval': instance.interval.toJson(),
  'nWindows': instance.nWindows,
  'nEvents': instance.nEvents,
  'periodFrom': instance.periodFrom.toIso8601String(),
  'periodTo': instance.periodTo.toIso8601String(),
  'sourceMix': instance.sourceMix,
  'licences': ?instance.licences?.map((e) => e.toJson()).toList(),
  'unconditional': ?instance.unconditional?.toJson(),
  'regionProfiles': ?instance.regionProfiles?.map((e) => e.toJson()).toList(),
  'caveats': instance.caveats.map((e) => e.toJson()).toList(),
  'computer': instance.computer.toJson(),
  'computedAt': instance.computedAt.toIso8601String(),
  'origin': _$BaseRateOriginEnumEnumMap[instance.origin]!,
  'cached': ?instance.cached,
  'manifestId': instance.manifestId,
};

const _$BaseRateOriginEnumEnumMap = {
  BaseRateOriginEnum.auto: 'auto',
  BaseRateOriginEnum.operator_: 'operator',
  BaseRateOriginEnum.agent: 'agent',
};
