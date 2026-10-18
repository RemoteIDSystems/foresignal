// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_cloud_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkCloudEventCWProxy {
  SinkCloudEvent specversion(SinkCloudEventSpecversionEnum specversion);

  SinkCloudEvent id(String id);

  SinkCloudEvent source_(String source_);

  SinkCloudEvent type(SinkCloudEventTypeEnum type);

  SinkCloudEvent subject(String subject);

  SinkCloudEvent time(DateTime time);

  SinkCloudEvent datacontenttype(
    SinkCloudEventDatacontenttypeEnum? datacontenttype,
  );

  SinkCloudEvent foresignalseq(int? foresignalseq);

  SinkCloudEvent foresignalkind(String? foresignalkind);

  SinkCloudEvent foresignalreason(String? foresignalreason);

  SinkCloudEvent data(SinkCloudEventData data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkCloudEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkCloudEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkCloudEvent call({
    SinkCloudEventSpecversionEnum specversion,
    String id,
    String source_,
    SinkCloudEventTypeEnum type,
    String subject,
    DateTime time,
    SinkCloudEventDatacontenttypeEnum? datacontenttype,
    int? foresignalseq,
    String? foresignalkind,
    String? foresignalreason,
    SinkCloudEventData data,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkCloudEvent.copyWith(...)` or call `instanceOfSinkCloudEvent.copyWith.fieldName(value)` for a single field.
class _$SinkCloudEventCWProxyImpl implements _$SinkCloudEventCWProxy {
  const _$SinkCloudEventCWProxyImpl(this._value);

  final SinkCloudEvent _value;

  @override
  SinkCloudEvent specversion(SinkCloudEventSpecversionEnum specversion) =>
      call(specversion: specversion);

  @override
  SinkCloudEvent id(String id) => call(id: id);

  @override
  SinkCloudEvent source_(String source_) => call(source_: source_);

  @override
  SinkCloudEvent type(SinkCloudEventTypeEnum type) => call(type: type);

  @override
  SinkCloudEvent subject(String subject) => call(subject: subject);

  @override
  SinkCloudEvent time(DateTime time) => call(time: time);

  @override
  SinkCloudEvent datacontenttype(
    SinkCloudEventDatacontenttypeEnum? datacontenttype,
  ) => call(datacontenttype: datacontenttype);

  @override
  SinkCloudEvent foresignalseq(int? foresignalseq) =>
      call(foresignalseq: foresignalseq);

  @override
  SinkCloudEvent foresignalkind(String? foresignalkind) =>
      call(foresignalkind: foresignalkind);

  @override
  SinkCloudEvent foresignalreason(String? foresignalreason) =>
      call(foresignalreason: foresignalreason);

  @override
  SinkCloudEvent data(SinkCloudEventData data) => call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkCloudEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkCloudEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkCloudEvent call({
    Object? specversion = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? subject = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? datacontenttype = const $CopyWithPlaceholder(),
    Object? foresignalseq = const $CopyWithPlaceholder(),
    Object? foresignalkind = const $CopyWithPlaceholder(),
    Object? foresignalreason = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return SinkCloudEvent(
      specversion:
          specversion == const $CopyWithPlaceholder() || specversion == null
          ? _value.specversion
          // ignore: cast_nullable_to_non_nullable
          : specversion as SinkCloudEventSpecversionEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SinkCloudEventTypeEnum,
      subject: subject == const $CopyWithPlaceholder() || subject == null
          ? _value.subject
          // ignore: cast_nullable_to_non_nullable
          : subject as String,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      datacontenttype: datacontenttype == const $CopyWithPlaceholder()
          ? _value.datacontenttype
          // ignore: cast_nullable_to_non_nullable
          : datacontenttype as SinkCloudEventDatacontenttypeEnum?,
      foresignalseq: foresignalseq == const $CopyWithPlaceholder()
          ? _value.foresignalseq
          // ignore: cast_nullable_to_non_nullable
          : foresignalseq as int?,
      foresignalkind: foresignalkind == const $CopyWithPlaceholder()
          ? _value.foresignalkind
          // ignore: cast_nullable_to_non_nullable
          : foresignalkind as String?,
      foresignalreason: foresignalreason == const $CopyWithPlaceholder()
          ? _value.foresignalreason
          // ignore: cast_nullable_to_non_nullable
          : foresignalreason as String?,
      data: data == const $CopyWithPlaceholder() || data == null
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as SinkCloudEventData,
    );
  }
}

extension $SinkCloudEventCopyWith on SinkCloudEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkCloudEvent.copyWith(...)` or `instanceOfSinkCloudEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkCloudEventCWProxy get copyWith => _$SinkCloudEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkCloudEvent _$SinkCloudEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SinkCloudEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'specversion',
      'id',
      'source',
      'type',
      'subject',
      'time',
      'data',
    ],
  );
  final val = SinkCloudEvent(
    specversion: $checkedConvert(
      'specversion',
      (v) => $enumDecode(_$SinkCloudEventSpecversionEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    source_: $checkedConvert('source', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$SinkCloudEventTypeEnumEnumMap, v),
    ),
    subject: $checkedConvert('subject', (v) => v as String),
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    datacontenttype: $checkedConvert(
      'datacontenttype',
      (v) => $enumDecodeNullable(_$SinkCloudEventDatacontenttypeEnumEnumMap, v),
    ),
    foresignalseq: $checkedConvert(
      'foresignalseq',
      (v) => (v as num?)?.toInt(),
    ),
    foresignalkind: $checkedConvert('foresignalkind', (v) => v as String?),
    foresignalreason: $checkedConvert('foresignalreason', (v) => v as String?),
    data: $checkedConvert(
      'data',
      (v) => SinkCloudEventData.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$SinkCloudEventToJson(
  SinkCloudEvent instance,
) => <String, dynamic>{
  'specversion': _$SinkCloudEventSpecversionEnumEnumMap[instance.specversion]!,
  'id': instance.id,
  'source': instance.source_,
  'type': _$SinkCloudEventTypeEnumEnumMap[instance.type]!,
  'subject': instance.subject,
  'time': instance.time.toIso8601String(),
  'datacontenttype':
      ?_$SinkCloudEventDatacontenttypeEnumEnumMap[instance.datacontenttype],
  'foresignalseq': ?instance.foresignalseq,
  'foresignalkind': ?instance.foresignalkind,
  'foresignalreason': ?instance.foresignalreason,
  'data': instance.data.toJson(),
};

const _$SinkCloudEventSpecversionEnumEnumMap = {
  SinkCloudEventSpecversionEnum.n1period0: '1.0',
};

const _$SinkCloudEventTypeEnumEnumMap = {
  SinkCloudEventTypeEnum.aiPeriodForesignalPeriodRecordPeriodCreated:
      'ai.foresignal.record.created',
  SinkCloudEventTypeEnum.aiPeriodForesignalPeriodRecordPeriodUpdated:
      'ai.foresignal.record.updated',
  SinkCloudEventTypeEnum.aiPeriodForesignalPeriodRecordPeriodTombstoned:
      'ai.foresignal.record.tombstoned',
  SinkCloudEventTypeEnum.aiPeriodForesignalPeriodRecordPeriodSnapshot:
      'ai.foresignal.record.snapshot',
};

const _$SinkCloudEventDatacontenttypeEnumEnumMap = {
  SinkCloudEventDatacontenttypeEnum.applicationSlashJson: 'application/json',
};
