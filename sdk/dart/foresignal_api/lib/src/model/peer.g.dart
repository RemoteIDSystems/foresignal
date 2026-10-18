// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerCWProxy {
  Peer id(String id);

  Peer name(String name);

  Peer baseUrl(String baseUrl);

  Peer mode(PeerModeEnum mode);

  Peer enabled(bool enabled);

  Peer state(String state);

  Peer healthReason(PeerHealthReasonEnum? healthReason);

  Peer lastSeq(int? lastSeq);

  Peer lagSeq(int? lagSeq);

  Peer lastError(String? lastError);

  Peer pinnedKid(String? pinnedKid);

  Peer remoteInstanceId(String? remoteInstanceId);

  Peer selection(PeerSelection selection);

  Peer sameOperator(bool? sameOperator);

  Peer reliability(PeerReliabilityEnum? reliability);

  Peer tokenSet(bool? tokenSet);

  Peer ingestKeyPrefix(String? ingestKeyPrefix);

  Peer sourceIds(List<String>? sourceIds);

  Peer createdAt(DateTime createdAt);

  Peer updatedAt(DateTime updatedAt);

  Peer exchangeKeySet(bool? exchangeKeySet);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Peer(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Peer(...).copyWith(id: 12, name: "My name")
  /// ```
  Peer call({
    String id,
    String name,
    String baseUrl,
    PeerModeEnum mode,
    bool enabled,
    String state,
    PeerHealthReasonEnum? healthReason,
    int? lastSeq,
    int? lagSeq,
    String? lastError,
    String? pinnedKid,
    String? remoteInstanceId,
    PeerSelection selection,
    bool? sameOperator,
    PeerReliabilityEnum? reliability,
    bool? tokenSet,
    String? ingestKeyPrefix,
    List<String>? sourceIds,
    DateTime createdAt,
    DateTime updatedAt,
    bool? exchangeKeySet,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeer.copyWith(...)` or call `instanceOfPeer.copyWith.fieldName(value)` for a single field.
class _$PeerCWProxyImpl implements _$PeerCWProxy {
  const _$PeerCWProxyImpl(this._value);

  final Peer _value;

  @override
  Peer id(String id) => call(id: id);

  @override
  Peer name(String name) => call(name: name);

  @override
  Peer baseUrl(String baseUrl) => call(baseUrl: baseUrl);

  @override
  Peer mode(PeerModeEnum mode) => call(mode: mode);

  @override
  Peer enabled(bool enabled) => call(enabled: enabled);

  @override
  Peer state(String state) => call(state: state);

  @override
  Peer healthReason(PeerHealthReasonEnum? healthReason) =>
      call(healthReason: healthReason);

  @override
  Peer lastSeq(int? lastSeq) => call(lastSeq: lastSeq);

  @override
  Peer lagSeq(int? lagSeq) => call(lagSeq: lagSeq);

  @override
  Peer lastError(String? lastError) => call(lastError: lastError);

  @override
  Peer pinnedKid(String? pinnedKid) => call(pinnedKid: pinnedKid);

  @override
  Peer remoteInstanceId(String? remoteInstanceId) =>
      call(remoteInstanceId: remoteInstanceId);

  @override
  Peer selection(PeerSelection selection) => call(selection: selection);

  @override
  Peer sameOperator(bool? sameOperator) => call(sameOperator: sameOperator);

  @override
  Peer reliability(PeerReliabilityEnum? reliability) =>
      call(reliability: reliability);

  @override
  Peer tokenSet(bool? tokenSet) => call(tokenSet: tokenSet);

  @override
  Peer ingestKeyPrefix(String? ingestKeyPrefix) =>
      call(ingestKeyPrefix: ingestKeyPrefix);

  @override
  Peer sourceIds(List<String>? sourceIds) => call(sourceIds: sourceIds);

  @override
  Peer createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Peer updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  Peer exchangeKeySet(bool? exchangeKeySet) =>
      call(exchangeKeySet: exchangeKeySet);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Peer(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Peer(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Peer call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? baseUrl = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? healthReason = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? lagSeq = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? pinnedKid = const $CopyWithPlaceholder(),
    Object? remoteInstanceId = const $CopyWithPlaceholder(),
    Object? selection = const $CopyWithPlaceholder(),
    Object? sameOperator = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
    Object? tokenSet = const $CopyWithPlaceholder(),
    Object? ingestKeyPrefix = const $CopyWithPlaceholder(),
    Object? sourceIds = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? exchangeKeySet = const $CopyWithPlaceholder(),
  }) {
    return Peer(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      baseUrl: baseUrl == const $CopyWithPlaceholder() || baseUrl == null
          ? _value.baseUrl
          // ignore: cast_nullable_to_non_nullable
          : baseUrl as String,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as PeerModeEnum,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as String,
      healthReason: healthReason == const $CopyWithPlaceholder()
          ? _value.healthReason
          // ignore: cast_nullable_to_non_nullable
          : healthReason as PeerHealthReasonEnum?,
      lastSeq: lastSeq == const $CopyWithPlaceholder()
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int?,
      lagSeq: lagSeq == const $CopyWithPlaceholder()
          ? _value.lagSeq
          // ignore: cast_nullable_to_non_nullable
          : lagSeq as int?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      pinnedKid: pinnedKid == const $CopyWithPlaceholder()
          ? _value.pinnedKid
          // ignore: cast_nullable_to_non_nullable
          : pinnedKid as String?,
      remoteInstanceId: remoteInstanceId == const $CopyWithPlaceholder()
          ? _value.remoteInstanceId
          // ignore: cast_nullable_to_non_nullable
          : remoteInstanceId as String?,
      selection: selection == const $CopyWithPlaceholder() || selection == null
          ? _value.selection
          // ignore: cast_nullable_to_non_nullable
          : selection as PeerSelection,
      sameOperator: sameOperator == const $CopyWithPlaceholder()
          ? _value.sameOperator
          // ignore: cast_nullable_to_non_nullable
          : sameOperator as bool?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as PeerReliabilityEnum?,
      tokenSet: tokenSet == const $CopyWithPlaceholder()
          ? _value.tokenSet
          // ignore: cast_nullable_to_non_nullable
          : tokenSet as bool?,
      ingestKeyPrefix: ingestKeyPrefix == const $CopyWithPlaceholder()
          ? _value.ingestKeyPrefix
          // ignore: cast_nullable_to_non_nullable
          : ingestKeyPrefix as String?,
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      exchangeKeySet: exchangeKeySet == const $CopyWithPlaceholder()
          ? _value.exchangeKeySet
          // ignore: cast_nullable_to_non_nullable
          : exchangeKeySet as bool?,
    );
  }
}

extension $PeerCopyWith on Peer {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeer.copyWith(...)` or `instanceOfPeer.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerCWProxy get copyWith => _$PeerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Peer _$PeerFromJson(Map<String, dynamic> json) => $checkedCreate('Peer', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'baseUrl',
      'mode',
      'enabled',
      'state',
      'selection',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Peer(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    baseUrl: $checkedConvert('baseUrl', (v) => v as String),
    mode: $checkedConvert('mode', (v) => $enumDecode(_$PeerModeEnumEnumMap, v)),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    state: $checkedConvert('state', (v) => v as String),
    healthReason: $checkedConvert(
      'healthReason',
      (v) => $enumDecodeNullable(_$PeerHealthReasonEnumEnumMap, v),
    ),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num?)?.toInt()),
    lagSeq: $checkedConvert('lagSeq', (v) => (v as num?)?.toInt()),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    pinnedKid: $checkedConvert('pinnedKid', (v) => v as String?),
    remoteInstanceId: $checkedConvert('remoteInstanceId', (v) => v as String?),
    selection: $checkedConvert(
      'selection',
      (v) => PeerSelection.fromJson(v as Map<String, dynamic>),
    ),
    sameOperator: $checkedConvert('sameOperator', (v) => v as bool?),
    reliability: $checkedConvert(
      'reliability',
      (v) => $enumDecodeNullable(_$PeerReliabilityEnumEnumMap, v),
    ),
    tokenSet: $checkedConvert('tokenSet', (v) => v as bool?),
    ingestKeyPrefix: $checkedConvert('ingestKeyPrefix', (v) => v as String?),
    sourceIds: $checkedConvert(
      'sourceIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    exchangeKeySet: $checkedConvert('exchangeKeySet', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$PeerToJson(Peer instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'baseUrl': instance.baseUrl,
  'mode': _$PeerModeEnumEnumMap[instance.mode]!,
  'enabled': instance.enabled,
  'state': instance.state,
  'healthReason': ?_$PeerHealthReasonEnumEnumMap[instance.healthReason],
  'lastSeq': ?instance.lastSeq,
  'lagSeq': ?instance.lagSeq,
  'lastError': ?instance.lastError,
  'pinnedKid': ?instance.pinnedKid,
  'remoteInstanceId': ?instance.remoteInstanceId,
  'selection': instance.selection.toJson(),
  'sameOperator': ?instance.sameOperator,
  'reliability': ?_$PeerReliabilityEnumEnumMap[instance.reliability],
  'tokenSet': ?instance.tokenSet,
  'ingestKeyPrefix': ?instance.ingestKeyPrefix,
  'sourceIds': ?instance.sourceIds,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
  'exchangeKeySet': ?instance.exchangeKeySet,
};

const _$PeerModeEnumEnumMap = {
  PeerModeEnum.PULL: 'PULL',
  PeerModeEnum.RECEIVE: 'RECEIVE',
};

const _$PeerHealthReasonEnumEnumMap = {
  PeerHealthReasonEnum.PEER_KEY_MISMATCH: 'PEER_KEY_MISMATCH',
  PeerHealthReasonEnum.PEER_UNREACHABLE: 'PEER_UNREACHABLE',
  PeerHealthReasonEnum.PEER_RESYNC: 'PEER_RESYNC',
};

const _$PeerReliabilityEnumEnumMap = {
  PeerReliabilityEnum.A: 'A',
  PeerReliabilityEnum.B: 'B',
  PeerReliabilityEnum.C: 'C',
  PeerReliabilityEnum.D: 'D',
  PeerReliabilityEnum.E: 'E',
  PeerReliabilityEnum.F: 'F',
};
