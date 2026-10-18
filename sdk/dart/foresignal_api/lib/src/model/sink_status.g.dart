// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkStatusCWProxy {
  SinkStatus sinkId(String sinkId);

  SinkStatus state(SinkStatusStateEnum state);

  SinkStatus lastSeq(int? lastSeq);

  SinkStatus watermarkSeq(int? watermarkSeq);

  SinkStatus lastPublishedSeq(int? lastPublishedSeq);

  SinkStatus lagSeq(int? lagSeq);

  SinkStatus lastDeliveredAt(DateTime? lastDeliveredAt);

  SinkStatus lastError(String? lastError);

  SinkStatus failingSince(DateTime? failingSince);

  SinkStatus resyncCount(int? resyncCount);

  SinkStatus delivered(int delivered);

  SinkStatus dropped(int dropped);

  SinkStatus denied(SinkStatusDenied denied);

  SinkStatus clients(SinkStatusClients? clients);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkStatus call({
    String sinkId,
    SinkStatusStateEnum state,
    int? lastSeq,
    int? watermarkSeq,
    int? lastPublishedSeq,
    int? lagSeq,
    DateTime? lastDeliveredAt,
    String? lastError,
    DateTime? failingSince,
    int? resyncCount,
    int delivered,
    int dropped,
    SinkStatusDenied denied,
    SinkStatusClients? clients,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkStatus.copyWith(...)` or call `instanceOfSinkStatus.copyWith.fieldName(value)` for a single field.
class _$SinkStatusCWProxyImpl implements _$SinkStatusCWProxy {
  const _$SinkStatusCWProxyImpl(this._value);

  final SinkStatus _value;

  @override
  SinkStatus sinkId(String sinkId) => call(sinkId: sinkId);

  @override
  SinkStatus state(SinkStatusStateEnum state) => call(state: state);

  @override
  SinkStatus lastSeq(int? lastSeq) => call(lastSeq: lastSeq);

  @override
  SinkStatus watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  SinkStatus lastPublishedSeq(int? lastPublishedSeq) =>
      call(lastPublishedSeq: lastPublishedSeq);

  @override
  SinkStatus lagSeq(int? lagSeq) => call(lagSeq: lagSeq);

  @override
  SinkStatus lastDeliveredAt(DateTime? lastDeliveredAt) =>
      call(lastDeliveredAt: lastDeliveredAt);

  @override
  SinkStatus lastError(String? lastError) => call(lastError: lastError);

  @override
  SinkStatus failingSince(DateTime? failingSince) =>
      call(failingSince: failingSince);

  @override
  SinkStatus resyncCount(int? resyncCount) => call(resyncCount: resyncCount);

  @override
  SinkStatus delivered(int delivered) => call(delivered: delivered);

  @override
  SinkStatus dropped(int dropped) => call(dropped: dropped);

  @override
  SinkStatus denied(SinkStatusDenied denied) => call(denied: denied);

  @override
  SinkStatus clients(SinkStatusClients? clients) => call(clients: clients);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkStatus call({
    Object? sinkId = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? lastPublishedSeq = const $CopyWithPlaceholder(),
    Object? lagSeq = const $CopyWithPlaceholder(),
    Object? lastDeliveredAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? failingSince = const $CopyWithPlaceholder(),
    Object? resyncCount = const $CopyWithPlaceholder(),
    Object? delivered = const $CopyWithPlaceholder(),
    Object? dropped = const $CopyWithPlaceholder(),
    Object? denied = const $CopyWithPlaceholder(),
    Object? clients = const $CopyWithPlaceholder(),
  }) {
    return SinkStatus(
      sinkId: sinkId == const $CopyWithPlaceholder() || sinkId == null
          ? _value.sinkId
          // ignore: cast_nullable_to_non_nullable
          : sinkId as String,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as SinkStatusStateEnum,
      lastSeq: lastSeq == const $CopyWithPlaceholder()
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      lastPublishedSeq: lastPublishedSeq == const $CopyWithPlaceholder()
          ? _value.lastPublishedSeq
          // ignore: cast_nullable_to_non_nullable
          : lastPublishedSeq as int?,
      lagSeq: lagSeq == const $CopyWithPlaceholder()
          ? _value.lagSeq
          // ignore: cast_nullable_to_non_nullable
          : lagSeq as int?,
      lastDeliveredAt: lastDeliveredAt == const $CopyWithPlaceholder()
          ? _value.lastDeliveredAt
          // ignore: cast_nullable_to_non_nullable
          : lastDeliveredAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      failingSince: failingSince == const $CopyWithPlaceholder()
          ? _value.failingSince
          // ignore: cast_nullable_to_non_nullable
          : failingSince as DateTime?,
      resyncCount: resyncCount == const $CopyWithPlaceholder()
          ? _value.resyncCount
          // ignore: cast_nullable_to_non_nullable
          : resyncCount as int?,
      delivered: delivered == const $CopyWithPlaceholder() || delivered == null
          ? _value.delivered
          // ignore: cast_nullable_to_non_nullable
          : delivered as int,
      dropped: dropped == const $CopyWithPlaceholder() || dropped == null
          ? _value.dropped
          // ignore: cast_nullable_to_non_nullable
          : dropped as int,
      denied: denied == const $CopyWithPlaceholder() || denied == null
          ? _value.denied
          // ignore: cast_nullable_to_non_nullable
          : denied as SinkStatusDenied,
      clients: clients == const $CopyWithPlaceholder()
          ? _value.clients
          // ignore: cast_nullable_to_non_nullable
          : clients as SinkStatusClients?,
    );
  }
}

extension $SinkStatusCopyWith on SinkStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkStatus.copyWith(...)` or `instanceOfSinkStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkStatusCWProxy get copyWith => _$SinkStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkStatus _$SinkStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SinkStatus', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['sinkId', 'state', 'delivered', 'dropped', 'denied'],
  );
  final val = SinkStatus(
    sinkId: $checkedConvert('sinkId', (v) => v as String),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$SinkStatusStateEnumEnumMap, v),
    ),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num?)?.toInt()),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    lastPublishedSeq: $checkedConvert(
      'lastPublishedSeq',
      (v) => (v as num?)?.toInt(),
    ),
    lagSeq: $checkedConvert('lagSeq', (v) => (v as num?)?.toInt()),
    lastDeliveredAt: $checkedConvert(
      'lastDeliveredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    failingSince: $checkedConvert(
      'failingSince',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    resyncCount: $checkedConvert('resyncCount', (v) => (v as num?)?.toInt()),
    delivered: $checkedConvert('delivered', (v) => (v as num).toInt()),
    dropped: $checkedConvert('dropped', (v) => (v as num).toInt()),
    denied: $checkedConvert(
      'denied',
      (v) => SinkStatusDenied.fromJson(v as Map<String, dynamic>),
    ),
    clients: $checkedConvert(
      'clients',
      (v) => v == null
          ? null
          : SinkStatusClients.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SinkStatusToJson(SinkStatus instance) =>
    <String, dynamic>{
      'sinkId': instance.sinkId,
      'state': _$SinkStatusStateEnumEnumMap[instance.state]!,
      'lastSeq': ?instance.lastSeq,
      'watermarkSeq': ?instance.watermarkSeq,
      'lastPublishedSeq': ?instance.lastPublishedSeq,
      'lagSeq': ?instance.lagSeq,
      'lastDeliveredAt': ?instance.lastDeliveredAt?.toIso8601String(),
      'lastError': ?instance.lastError,
      'failingSince': ?instance.failingSince?.toIso8601String(),
      'resyncCount': ?instance.resyncCount,
      'delivered': instance.delivered,
      'dropped': instance.dropped,
      'denied': instance.denied.toJson(),
      'clients': ?instance.clients?.toJson(),
    };

const _$SinkStatusStateEnumEnumMap = {
  SinkStatusStateEnum.RUNNING: 'RUNNING',
  SinkStatusStateEnum.RESYNCING: 'RESYNCING',
  SinkStatusStateEnum.FAILING: 'FAILING',
  SinkStatusStateEnum.STOPPED: 'STOPPED',
};
