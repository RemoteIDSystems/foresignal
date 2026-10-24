// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorCWProxy {
  Precursor id(String id);

  Precursor kind(PrecursorKindEnum kind);

  Precursor antecedent(PrecursorSide antecedent);

  Precursor consequent(PrecursorSide consequent);

  Precursor spatial(PrecursorSpatial spatial);

  Precursor regionCodes(List<String>? regionCodes);

  Precursor lagMin(String lagMin);

  Precursor lagMax(String lagMax);

  Precursor bestLag(String? bestLag);

  Precursor lagDistribution(PrecursorLagDistribution? lagDistribution);

  Precursor hitRate(num? hitRate);

  Precursor hitRateInterval(StatInterval? hitRateInterval);

  Precursor baseRate(num? baseRate);

  Precursor lift(num? lift);

  Precursor correlation(num? correlation);

  Precursor correlationSign(PrecursorCorrelationSignEnum? correlationSign);

  Precursor nAntecedent(int nAntecedent);

  Precursor discovery(PrecursorWindowStats discovery);

  Precursor validation(PrecursorWindowStats? validation);

  Precursor lastRetest(PrecursorWindowStats? lastRetest);

  Precursor batchId(String batchId);

  Precursor batchSize(int batchSize);

  Precursor status(PrecursorStatusEnum status);

  Precursor statusReason(String? statusReason);

  Precursor operatorValidated(bool? operatorValidated);

  Precursor detector(DetectorRef detector);

  Precursor firstValidatedAt(DateTime? firstValidatedAt);

  Precursor lastTestedAt(DateTime? lastTestedAt);

  Precursor origin(PrecursorOriginEnum origin);

  Precursor description(String description);

  Precursor associationNote(PrecursorAssociationNoteEnum associationNote);

  Precursor caveats(List<Caveat> caveats);

  Precursor history(List<PrecursorHistoryInner>? history);

  Precursor manifestId(String manifestId);

  Precursor createdAt(DateTime? createdAt);

  Precursor updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Precursor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Precursor(...).copyWith(id: 12, name: "My name")
  /// ```
  Precursor call({
    String id,
    PrecursorKindEnum kind,
    PrecursorSide antecedent,
    PrecursorSide consequent,
    PrecursorSpatial spatial,
    List<String>? regionCodes,
    String lagMin,
    String lagMax,
    String? bestLag,
    PrecursorLagDistribution? lagDistribution,
    num? hitRate,
    StatInterval? hitRateInterval,
    num? baseRate,
    num? lift,
    num? correlation,
    PrecursorCorrelationSignEnum? correlationSign,
    int nAntecedent,
    PrecursorWindowStats discovery,
    PrecursorWindowStats? validation,
    PrecursorWindowStats? lastRetest,
    String batchId,
    int batchSize,
    PrecursorStatusEnum status,
    String? statusReason,
    bool? operatorValidated,
    DetectorRef detector,
    DateTime? firstValidatedAt,
    DateTime? lastTestedAt,
    PrecursorOriginEnum origin,
    String description,
    PrecursorAssociationNoteEnum associationNote,
    List<Caveat> caveats,
    List<PrecursorHistoryInner>? history,
    String manifestId,
    DateTime? createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursor.copyWith(...)` or call `instanceOfPrecursor.copyWith.fieldName(value)` for a single field.
class _$PrecursorCWProxyImpl implements _$PrecursorCWProxy {
  const _$PrecursorCWProxyImpl(this._value);

  final Precursor _value;

  @override
  Precursor id(String id) => call(id: id);

  @override
  Precursor kind(PrecursorKindEnum kind) => call(kind: kind);

  @override
  Precursor antecedent(PrecursorSide antecedent) =>
      call(antecedent: antecedent);

  @override
  Precursor consequent(PrecursorSide consequent) =>
      call(consequent: consequent);

  @override
  Precursor spatial(PrecursorSpatial spatial) => call(spatial: spatial);

  @override
  Precursor regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  Precursor lagMin(String lagMin) => call(lagMin: lagMin);

  @override
  Precursor lagMax(String lagMax) => call(lagMax: lagMax);

  @override
  Precursor bestLag(String? bestLag) => call(bestLag: bestLag);

  @override
  Precursor lagDistribution(PrecursorLagDistribution? lagDistribution) =>
      call(lagDistribution: lagDistribution);

  @override
  Precursor hitRate(num? hitRate) => call(hitRate: hitRate);

  @override
  Precursor hitRateInterval(StatInterval? hitRateInterval) =>
      call(hitRateInterval: hitRateInterval);

  @override
  Precursor baseRate(num? baseRate) => call(baseRate: baseRate);

  @override
  Precursor lift(num? lift) => call(lift: lift);

  @override
  Precursor correlation(num? correlation) => call(correlation: correlation);

  @override
  Precursor correlationSign(PrecursorCorrelationSignEnum? correlationSign) =>
      call(correlationSign: correlationSign);

  @override
  Precursor nAntecedent(int nAntecedent) => call(nAntecedent: nAntecedent);

  @override
  Precursor discovery(PrecursorWindowStats discovery) =>
      call(discovery: discovery);

  @override
  Precursor validation(PrecursorWindowStats? validation) =>
      call(validation: validation);

  @override
  Precursor lastRetest(PrecursorWindowStats? lastRetest) =>
      call(lastRetest: lastRetest);

  @override
  Precursor batchId(String batchId) => call(batchId: batchId);

  @override
  Precursor batchSize(int batchSize) => call(batchSize: batchSize);

  @override
  Precursor status(PrecursorStatusEnum status) => call(status: status);

  @override
  Precursor statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  Precursor operatorValidated(bool? operatorValidated) =>
      call(operatorValidated: operatorValidated);

  @override
  Precursor detector(DetectorRef detector) => call(detector: detector);

  @override
  Precursor firstValidatedAt(DateTime? firstValidatedAt) =>
      call(firstValidatedAt: firstValidatedAt);

  @override
  Precursor lastTestedAt(DateTime? lastTestedAt) =>
      call(lastTestedAt: lastTestedAt);

  @override
  Precursor origin(PrecursorOriginEnum origin) => call(origin: origin);

  @override
  Precursor description(String description) => call(description: description);

  @override
  Precursor associationNote(PrecursorAssociationNoteEnum associationNote) =>
      call(associationNote: associationNote);

  @override
  Precursor caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  Precursor history(List<PrecursorHistoryInner>? history) =>
      call(history: history);

  @override
  Precursor manifestId(String manifestId) => call(manifestId: manifestId);

  @override
  Precursor createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  Precursor updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Precursor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Precursor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Precursor call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? antecedent = const $CopyWithPlaceholder(),
    Object? consequent = const $CopyWithPlaceholder(),
    Object? spatial = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? lagMin = const $CopyWithPlaceholder(),
    Object? lagMax = const $CopyWithPlaceholder(),
    Object? bestLag = const $CopyWithPlaceholder(),
    Object? lagDistribution = const $CopyWithPlaceholder(),
    Object? hitRate = const $CopyWithPlaceholder(),
    Object? hitRateInterval = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? lift = const $CopyWithPlaceholder(),
    Object? correlation = const $CopyWithPlaceholder(),
    Object? correlationSign = const $CopyWithPlaceholder(),
    Object? nAntecedent = const $CopyWithPlaceholder(),
    Object? discovery = const $CopyWithPlaceholder(),
    Object? validation = const $CopyWithPlaceholder(),
    Object? lastRetest = const $CopyWithPlaceholder(),
    Object? batchId = const $CopyWithPlaceholder(),
    Object? batchSize = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? operatorValidated = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? firstValidatedAt = const $CopyWithPlaceholder(),
    Object? lastTestedAt = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? associationNote = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? history = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Precursor(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PrecursorKindEnum,
      antecedent:
          antecedent == const $CopyWithPlaceholder() || antecedent == null
          ? _value.antecedent
          // ignore: cast_nullable_to_non_nullable
          : antecedent as PrecursorSide,
      consequent:
          consequent == const $CopyWithPlaceholder() || consequent == null
          ? _value.consequent
          // ignore: cast_nullable_to_non_nullable
          : consequent as PrecursorSide,
      spatial: spatial == const $CopyWithPlaceholder() || spatial == null
          ? _value.spatial
          // ignore: cast_nullable_to_non_nullable
          : spatial as PrecursorSpatial,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      lagMin: lagMin == const $CopyWithPlaceholder() || lagMin == null
          ? _value.lagMin
          // ignore: cast_nullable_to_non_nullable
          : lagMin as String,
      lagMax: lagMax == const $CopyWithPlaceholder() || lagMax == null
          ? _value.lagMax
          // ignore: cast_nullable_to_non_nullable
          : lagMax as String,
      bestLag: bestLag == const $CopyWithPlaceholder()
          ? _value.bestLag
          // ignore: cast_nullable_to_non_nullable
          : bestLag as String?,
      lagDistribution: lagDistribution == const $CopyWithPlaceholder()
          ? _value.lagDistribution
          // ignore: cast_nullable_to_non_nullable
          : lagDistribution as PrecursorLagDistribution?,
      hitRate: hitRate == const $CopyWithPlaceholder()
          ? _value.hitRate
          // ignore: cast_nullable_to_non_nullable
          : hitRate as num?,
      hitRateInterval: hitRateInterval == const $CopyWithPlaceholder()
          ? _value.hitRateInterval
          // ignore: cast_nullable_to_non_nullable
          : hitRateInterval as StatInterval?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
      lift: lift == const $CopyWithPlaceholder()
          ? _value.lift
          // ignore: cast_nullable_to_non_nullable
          : lift as num?,
      correlation: correlation == const $CopyWithPlaceholder()
          ? _value.correlation
          // ignore: cast_nullable_to_non_nullable
          : correlation as num?,
      correlationSign: correlationSign == const $CopyWithPlaceholder()
          ? _value.correlationSign
          // ignore: cast_nullable_to_non_nullable
          : correlationSign as PrecursorCorrelationSignEnum?,
      nAntecedent:
          nAntecedent == const $CopyWithPlaceholder() || nAntecedent == null
          ? _value.nAntecedent
          // ignore: cast_nullable_to_non_nullable
          : nAntecedent as int,
      discovery: discovery == const $CopyWithPlaceholder() || discovery == null
          ? _value.discovery
          // ignore: cast_nullable_to_non_nullable
          : discovery as PrecursorWindowStats,
      validation: validation == const $CopyWithPlaceholder()
          ? _value.validation
          // ignore: cast_nullable_to_non_nullable
          : validation as PrecursorWindowStats?,
      lastRetest: lastRetest == const $CopyWithPlaceholder()
          ? _value.lastRetest
          // ignore: cast_nullable_to_non_nullable
          : lastRetest as PrecursorWindowStats?,
      batchId: batchId == const $CopyWithPlaceholder() || batchId == null
          ? _value.batchId
          // ignore: cast_nullable_to_non_nullable
          : batchId as String,
      batchSize: batchSize == const $CopyWithPlaceholder() || batchSize == null
          ? _value.batchSize
          // ignore: cast_nullable_to_non_nullable
          : batchSize as int,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PrecursorStatusEnum,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      operatorValidated: operatorValidated == const $CopyWithPlaceholder()
          ? _value.operatorValidated
          // ignore: cast_nullable_to_non_nullable
          : operatorValidated as bool?,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      firstValidatedAt: firstValidatedAt == const $CopyWithPlaceholder()
          ? _value.firstValidatedAt
          // ignore: cast_nullable_to_non_nullable
          : firstValidatedAt as DateTime?,
      lastTestedAt: lastTestedAt == const $CopyWithPlaceholder()
          ? _value.lastTestedAt
          // ignore: cast_nullable_to_non_nullable
          : lastTestedAt as DateTime?,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as PrecursorOriginEnum,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      associationNote:
          associationNote == const $CopyWithPlaceholder() ||
              associationNote == null
          ? _value.associationNote
          // ignore: cast_nullable_to_non_nullable
          : associationNote as PrecursorAssociationNoteEnum,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
      history: history == const $CopyWithPlaceholder()
          ? _value.history
          // ignore: cast_nullable_to_non_nullable
          : history as List<PrecursorHistoryInner>?,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $PrecursorCopyWith on Precursor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursor.copyWith(...)` or `instanceOfPrecursor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorCWProxy get copyWith => _$PrecursorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Precursor _$PrecursorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Precursor', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'kind',
      'antecedent',
      'consequent',
      'spatial',
      'lagMin',
      'lagMax',
      'nAntecedent',
      'discovery',
      'batchId',
      'batchSize',
      'status',
      'detector',
      'origin',
      'description',
      'associationNote',
      'caveats',
      'manifestId',
    ],
  );
  final val = Precursor(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$PrecursorKindEnumEnumMap, v),
    ),
    antecedent: $checkedConvert(
      'antecedent',
      (v) => PrecursorSide.fromJson(v as Map<String, dynamic>),
    ),
    consequent: $checkedConvert(
      'consequent',
      (v) => PrecursorSide.fromJson(v as Map<String, dynamic>),
    ),
    spatial: $checkedConvert(
      'spatial',
      (v) => PrecursorSpatial.fromJson(v as Map<String, dynamic>),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    lagMin: $checkedConvert('lagMin', (v) => v as String),
    lagMax: $checkedConvert('lagMax', (v) => v as String),
    bestLag: $checkedConvert('bestLag', (v) => v as String?),
    lagDistribution: $checkedConvert(
      'lagDistribution',
      (v) => v == null
          ? null
          : PrecursorLagDistribution.fromJson(v as Map<String, dynamic>),
    ),
    hitRate: $checkedConvert('hitRate', (v) => v as num?),
    hitRateInterval: $checkedConvert(
      'hitRateInterval',
      (v) =>
          v == null ? null : StatInterval.fromJson(v as Map<String, dynamic>),
    ),
    baseRate: $checkedConvert('baseRate', (v) => v as num?),
    lift: $checkedConvert('lift', (v) => v as num?),
    correlation: $checkedConvert('correlation', (v) => v as num?),
    correlationSign: $checkedConvert(
      'correlationSign',
      (v) => $enumDecodeNullable(_$PrecursorCorrelationSignEnumEnumMap, v),
    ),
    nAntecedent: $checkedConvert('nAntecedent', (v) => (v as num).toInt()),
    discovery: $checkedConvert(
      'discovery',
      (v) => PrecursorWindowStats.fromJson(v as Map<String, dynamic>),
    ),
    validation: $checkedConvert(
      'validation',
      (v) => v == null
          ? null
          : PrecursorWindowStats.fromJson(v as Map<String, dynamic>),
    ),
    lastRetest: $checkedConvert(
      'lastRetest',
      (v) => v == null
          ? null
          : PrecursorWindowStats.fromJson(v as Map<String, dynamic>),
    ),
    batchId: $checkedConvert('batchId', (v) => v as String),
    batchSize: $checkedConvert('batchSize', (v) => (v as num).toInt()),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$PrecursorStatusEnumEnumMap, v),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    operatorValidated: $checkedConvert('operatorValidated', (v) => v as bool?),
    detector: $checkedConvert(
      'detector',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    firstValidatedAt: $checkedConvert(
      'firstValidatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastTestedAt: $checkedConvert(
      'lastTestedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$PrecursorOriginEnumEnumMap, v),
    ),
    description: $checkedConvert('description', (v) => v as String),
    associationNote: $checkedConvert(
      'associationNote',
      (v) => $enumDecode(_$PrecursorAssociationNoteEnumEnumMap, v),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>)
          .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    history: $checkedConvert(
      'history',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => PrecursorHistoryInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorToJson(Precursor instance) => <String, dynamic>{
  'id': instance.id,
  'kind': _$PrecursorKindEnumEnumMap[instance.kind]!,
  'antecedent': instance.antecedent.toJson(),
  'consequent': instance.consequent.toJson(),
  'spatial': instance.spatial.toJson(),
  'regionCodes': ?instance.regionCodes,
  'lagMin': instance.lagMin,
  'lagMax': instance.lagMax,
  'bestLag': ?instance.bestLag,
  'lagDistribution': ?instance.lagDistribution?.toJson(),
  'hitRate': ?instance.hitRate,
  'hitRateInterval': ?instance.hitRateInterval?.toJson(),
  'baseRate': ?instance.baseRate,
  'lift': ?instance.lift,
  'correlation': ?instance.correlation,
  'correlationSign':
      ?_$PrecursorCorrelationSignEnumEnumMap[instance.correlationSign],
  'nAntecedent': instance.nAntecedent,
  'discovery': instance.discovery.toJson(),
  'validation': ?instance.validation?.toJson(),
  'lastRetest': ?instance.lastRetest?.toJson(),
  'batchId': instance.batchId,
  'batchSize': instance.batchSize,
  'status': _$PrecursorStatusEnumEnumMap[instance.status]!,
  'statusReason': ?instance.statusReason,
  'operatorValidated': ?instance.operatorValidated,
  'detector': instance.detector.toJson(),
  'firstValidatedAt': ?instance.firstValidatedAt?.toIso8601String(),
  'lastTestedAt': ?instance.lastTestedAt?.toIso8601String(),
  'origin': _$PrecursorOriginEnumEnumMap[instance.origin]!,
  'description': instance.description,
  'associationNote':
      _$PrecursorAssociationNoteEnumEnumMap[instance.associationNote]!,
  'caveats': instance.caveats.map((e) => e.toJson()).toList(),
  'history': ?instance.history?.map((e) => e.toJson()).toList(),
  'manifestId': instance.manifestId,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$PrecursorKindEnumEnumMap = {
  PrecursorKindEnum.event: 'event',
  PrecursorKindEnum.series: 'series',
};

const _$PrecursorCorrelationSignEnumEnumMap = {
  PrecursorCorrelationSignEnum.positive: 'positive',
  PrecursorCorrelationSignEnum.negative: 'negative',
};

const _$PrecursorStatusEnumEnumMap = {
  PrecursorStatusEnum.candidate: 'candidate',
  PrecursorStatusEnum.validated: 'validated',
  PrecursorStatusEnum.stale: 'stale',
  PrecursorStatusEnum.retired: 'retired',
};

const _$PrecursorOriginEnumEnumMap = {
  PrecursorOriginEnum.auto: 'auto',
  PrecursorOriginEnum.operator_: 'operator',
};

const _$PrecursorAssociationNoteEnumEnumMap = {
  PrecursorAssociationNoteEnum.associationInStoredDataSemicolonNotCausation:
      'association in stored data; not causation',
};
