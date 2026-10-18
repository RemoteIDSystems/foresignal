// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cloud_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CloudEventCWProxy {
  CloudEvent specversion(CloudEventSpecversionEnum specversion);

  CloudEvent id(String id);

  CloudEvent source_(String source_);

  CloudEvent type(String type);

  CloudEvent subject(String? subject);

  CloudEvent time(DateTime? time);

  CloudEvent datacontenttype(String? datacontenttype);

  CloudEvent dataschema(String? dataschema);

  CloudEvent data(Object? data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CloudEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CloudEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  CloudEvent call({
    CloudEventSpecversionEnum specversion,
    String id,
    String source_,
    String type,
    String? subject,
    DateTime? time,
    String? datacontenttype,
    String? dataschema,
    Object? data,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCloudEvent.copyWith(...)` or call `instanceOfCloudEvent.copyWith.fieldName(value)` for a single field.
class _$CloudEventCWProxyImpl implements _$CloudEventCWProxy {
  const _$CloudEventCWProxyImpl(this._value);

  final CloudEvent _value;

  @override
  CloudEvent specversion(CloudEventSpecversionEnum specversion) =>
      call(specversion: specversion);

  @override
  CloudEvent id(String id) => call(id: id);

  @override
  CloudEvent source_(String source_) => call(source_: source_);

  @override
  CloudEvent type(String type) => call(type: type);

  @override
  CloudEvent subject(String? subject) => call(subject: subject);

  @override
  CloudEvent time(DateTime? time) => call(time: time);

  @override
  CloudEvent datacontenttype(String? datacontenttype) =>
      call(datacontenttype: datacontenttype);

  @override
  CloudEvent dataschema(String? dataschema) => call(dataschema: dataschema);

  @override
  CloudEvent data(Object? data) => call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CloudEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CloudEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CloudEvent call({
    Object? specversion = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? subject = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? datacontenttype = const $CopyWithPlaceholder(),
    Object? dataschema = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return CloudEvent(
      specversion:
          specversion == const $CopyWithPlaceholder() || specversion == null
          ? _value.specversion
          // ignore: cast_nullable_to_non_nullable
          : specversion as CloudEventSpecversionEnum,
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
          : type as String,
      subject: subject == const $CopyWithPlaceholder()
          ? _value.subject
          // ignore: cast_nullable_to_non_nullable
          : subject as String?,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime?,
      datacontenttype: datacontenttype == const $CopyWithPlaceholder()
          ? _value.datacontenttype
          // ignore: cast_nullable_to_non_nullable
          : datacontenttype as String?,
      dataschema: dataschema == const $CopyWithPlaceholder()
          ? _value.dataschema
          // ignore: cast_nullable_to_non_nullable
          : dataschema as String?,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Object?,
    );
  }
}

extension $CloudEventCopyWith on CloudEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCloudEvent.copyWith(...)` or `instanceOfCloudEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CloudEventCWProxy get copyWith => _$CloudEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CloudEvent _$CloudEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CloudEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['specversion', 'id', 'source', 'type']);
  final val = CloudEvent(
    specversion: $checkedConvert(
      'specversion',
      (v) => $enumDecode(_$CloudEventSpecversionEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    source_: $checkedConvert('source', (v) => v as String),
    type: $checkedConvert('type', (v) => v as String),
    subject: $checkedConvert('subject', (v) => v as String?),
    time: $checkedConvert(
      'time',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    datacontenttype: $checkedConvert('datacontenttype', (v) => v as String?),
    dataschema: $checkedConvert('dataschema', (v) => v as String?),
    data: $checkedConvert('data', (v) => v),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$CloudEventToJson(CloudEvent instance) =>
    <String, dynamic>{
      'specversion': _$CloudEventSpecversionEnumEnumMap[instance.specversion]!,
      'id': instance.id,
      'source': instance.source_,
      'type': instance.type,
      'subject': ?instance.subject,
      'time': ?instance.time?.toIso8601String(),
      'datacontenttype': ?instance.datacontenttype,
      'dataschema': ?instance.dataschema,
      'data': ?instance.data,
    };

const _$CloudEventSpecversionEnumEnumMap = {
  CloudEventSpecversionEnum.n1period0: '1.0',
};
