// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'provenance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProvenanceCWProxy {
  Provenance sourceIds(List<String>? sourceIds);

  Provenance manifestId(String? manifestId);

  Provenance asOfSeq(int? asOfSeq);

  Provenance licence(LicenceTerms? licence);

  Provenance licenceBasis(ProvenanceLicenceBasisEnum? licenceBasis);

  Provenance fetchId(int? fetchId);

  Provenance adapterVersion(String? adapterVersion);

  Provenance principal(String? principal);

  Provenance reliability(ProvenanceReliabilityEnum? reliability);

  Provenance credibility(int? credibility);

  Provenance grade(String? grade);

  Provenance origin(ProvenanceHop? origin);

  Provenance chain(List<ProvenanceHop>? chain);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Provenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Provenance(...).copyWith(id: 12, name: "My name")
  /// ```
  Provenance call({
    List<String>? sourceIds,
    String? manifestId,
    int? asOfSeq,
    LicenceTerms? licence,
    ProvenanceLicenceBasisEnum? licenceBasis,
    int? fetchId,
    String? adapterVersion,
    String? principal,
    ProvenanceReliabilityEnum? reliability,
    int? credibility,
    String? grade,
    ProvenanceHop? origin,
    List<ProvenanceHop>? chain,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProvenance.copyWith(...)` or call `instanceOfProvenance.copyWith.fieldName(value)` for a single field.
class _$ProvenanceCWProxyImpl implements _$ProvenanceCWProxy {
  const _$ProvenanceCWProxyImpl(this._value);

  final Provenance _value;

  @override
  Provenance sourceIds(List<String>? sourceIds) => call(sourceIds: sourceIds);

  @override
  Provenance manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  Provenance asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  Provenance licence(LicenceTerms? licence) => call(licence: licence);

  @override
  Provenance licenceBasis(ProvenanceLicenceBasisEnum? licenceBasis) =>
      call(licenceBasis: licenceBasis);

  @override
  Provenance fetchId(int? fetchId) => call(fetchId: fetchId);

  @override
  Provenance adapterVersion(String? adapterVersion) =>
      call(adapterVersion: adapterVersion);

  @override
  Provenance principal(String? principal) => call(principal: principal);

  @override
  Provenance reliability(ProvenanceReliabilityEnum? reliability) =>
      call(reliability: reliability);

  @override
  Provenance credibility(int? credibility) => call(credibility: credibility);

  @override
  Provenance grade(String? grade) => call(grade: grade);

  @override
  Provenance origin(ProvenanceHop? origin) => call(origin: origin);

  @override
  Provenance chain(List<ProvenanceHop>? chain) => call(chain: chain);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Provenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Provenance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Provenance call({
    Object? sourceIds = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? licenceBasis = const $CopyWithPlaceholder(),
    Object? fetchId = const $CopyWithPlaceholder(),
    Object? adapterVersion = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? grade = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? chain = const $CopyWithPlaceholder(),
  }) {
    return Provenance(
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as LicenceTerms?,
      licenceBasis: licenceBasis == const $CopyWithPlaceholder()
          ? _value.licenceBasis
          // ignore: cast_nullable_to_non_nullable
          : licenceBasis as ProvenanceLicenceBasisEnum?,
      fetchId: fetchId == const $CopyWithPlaceholder()
          ? _value.fetchId
          // ignore: cast_nullable_to_non_nullable
          : fetchId as int?,
      adapterVersion: adapterVersion == const $CopyWithPlaceholder()
          ? _value.adapterVersion
          // ignore: cast_nullable_to_non_nullable
          : adapterVersion as String?,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as String?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as ProvenanceReliabilityEnum?,
      credibility: credibility == const $CopyWithPlaceholder()
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as int?,
      grade: grade == const $CopyWithPlaceholder()
          ? _value.grade
          // ignore: cast_nullable_to_non_nullable
          : grade as String?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as ProvenanceHop?,
      chain: chain == const $CopyWithPlaceholder()
          ? _value.chain
          // ignore: cast_nullable_to_non_nullable
          : chain as List<ProvenanceHop>?,
    );
  }
}

extension $ProvenanceCopyWith on Provenance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProvenance.copyWith(...)` or `instanceOfProvenance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProvenanceCWProxy get copyWith => _$ProvenanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Provenance _$ProvenanceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Provenance', json, ($checkedConvert) {
  final val = Provenance(
    sourceIds: $checkedConvert(
      'sourceIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
    licence: $checkedConvert(
      'licence',
      (v) =>
          v == null ? null : LicenceTerms.fromJson(v as Map<String, dynamic>),
    ),
    licenceBasis: $checkedConvert(
      'licenceBasis',
      (v) => $enumDecodeNullable(_$ProvenanceLicenceBasisEnumEnumMap, v),
    ),
    fetchId: $checkedConvert('fetchId', (v) => (v as num?)?.toInt()),
    adapterVersion: $checkedConvert('adapterVersion', (v) => v as String?),
    principal: $checkedConvert('principal', (v) => v as String?),
    reliability: $checkedConvert(
      'reliability',
      (v) => $enumDecodeNullable(_$ProvenanceReliabilityEnumEnumMap, v),
    ),
    credibility: $checkedConvert('credibility', (v) => (v as num?)?.toInt()),
    grade: $checkedConvert('grade', (v) => v as String?),
    origin: $checkedConvert(
      'origin',
      (v) =>
          v == null ? null : ProvenanceHop.fromJson(v as Map<String, dynamic>),
    ),
    chain: $checkedConvert(
      'chain',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProvenanceHop.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ProvenanceToJson(
  Provenance instance,
) => <String, dynamic>{
  'sourceIds': ?instance.sourceIds,
  'manifestId': ?instance.manifestId,
  'asOfSeq': ?instance.asOfSeq,
  'licence': ?instance.licence?.toJson(),
  'licenceBasis': ?_$ProvenanceLicenceBasisEnumEnumMap[instance.licenceBasis],
  'fetchId': ?instance.fetchId,
  'adapterVersion': ?instance.adapterVersion,
  'principal': ?instance.principal,
  'reliability': ?_$ProvenanceReliabilityEnumEnumMap[instance.reliability],
  'credibility': ?instance.credibility,
  'grade': ?instance.grade,
  'origin': ?instance.origin?.toJson(),
  'chain': ?instance.chain?.map((e) => e.toJson()).toList(),
};

const _$ProvenanceLicenceBasisEnumEnumMap = {
  ProvenanceLicenceBasisEnum.batch: 'batch',
  ProvenanceLicenceBasisEnum.descriptor: 'descriptor',
  ProvenanceLicenceBasisEnum.mlatOverride: 'mlat-override',
  ProvenanceLicenceBasisEnum.peerOrigin: 'peer-origin',
};

const _$ProvenanceReliabilityEnumEnumMap = {
  ProvenanceReliabilityEnum.A: 'A',
  ProvenanceReliabilityEnum.B: 'B',
  ProvenanceReliabilityEnum.C: 'C',
  ProvenanceReliabilityEnum.D: 'D',
  ProvenanceReliabilityEnum.E: 'E',
  ProvenanceReliabilityEnum.F: 'F',
};
