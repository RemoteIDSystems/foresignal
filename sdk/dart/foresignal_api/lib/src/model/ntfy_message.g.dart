// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ntfy_message.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NtfyMessageCWProxy {
  NtfyMessage id(String id);

  NtfyMessage time(int time);

  NtfyMessage event(NtfyMessageEventEnum event);

  NtfyMessage topic(String topic);

  NtfyMessage message(String? message);

  NtfyMessage title(String? title);

  NtfyMessage priority(int? priority);

  NtfyMessage tags(List<String>? tags);

  NtfyMessage click(String? click);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NtfyMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NtfyMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  NtfyMessage call({
    String id,
    int time,
    NtfyMessageEventEnum event,
    String topic,
    String? message,
    String? title,
    int? priority,
    List<String>? tags,
    String? click,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNtfyMessage.copyWith(...)` or call `instanceOfNtfyMessage.copyWith.fieldName(value)` for a single field.
class _$NtfyMessageCWProxyImpl implements _$NtfyMessageCWProxy {
  const _$NtfyMessageCWProxyImpl(this._value);

  final NtfyMessage _value;

  @override
  NtfyMessage id(String id) => call(id: id);

  @override
  NtfyMessage time(int time) => call(time: time);

  @override
  NtfyMessage event(NtfyMessageEventEnum event) => call(event: event);

  @override
  NtfyMessage topic(String topic) => call(topic: topic);

  @override
  NtfyMessage message(String? message) => call(message: message);

  @override
  NtfyMessage title(String? title) => call(title: title);

  @override
  NtfyMessage priority(int? priority) => call(priority: priority);

  @override
  NtfyMessage tags(List<String>? tags) => call(tags: tags);

  @override
  NtfyMessage click(String? click) => call(click: click);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NtfyMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NtfyMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NtfyMessage call({
    Object? id = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? topic = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? priority = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? click = const $CopyWithPlaceholder(),
  }) {
    return NtfyMessage(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as NtfyMessageEventEnum,
      topic: topic == const $CopyWithPlaceholder() || topic == null
          ? _value.topic
          // ignore: cast_nullable_to_non_nullable
          : topic as String,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      priority: priority == const $CopyWithPlaceholder()
          ? _value.priority
          // ignore: cast_nullable_to_non_nullable
          : priority as int?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      click: click == const $CopyWithPlaceholder()
          ? _value.click
          // ignore: cast_nullable_to_non_nullable
          : click as String?,
    );
  }
}

extension $NtfyMessageCopyWith on NtfyMessage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNtfyMessage.copyWith(...)` or `instanceOfNtfyMessage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NtfyMessageCWProxy get copyWith => _$NtfyMessageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NtfyMessage _$NtfyMessageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NtfyMessage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'time', 'event', 'topic']);
      final val = NtfyMessage(
        id: $checkedConvert('id', (v) => v as String),
        time: $checkedConvert('time', (v) => (v as num).toInt()),
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$NtfyMessageEventEnumEnumMap, v),
        ),
        topic: $checkedConvert('topic', (v) => v as String),
        message: $checkedConvert('message', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String?),
        priority: $checkedConvert('priority', (v) => (v as num?)?.toInt()),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        click: $checkedConvert('click', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NtfyMessageToJson(NtfyMessage instance) =>
    <String, dynamic>{
      'id': instance.id,
      'time': instance.time,
      'event': _$NtfyMessageEventEnumEnumMap[instance.event]!,
      'topic': instance.topic,
      'message': ?instance.message,
      'title': ?instance.title,
      'priority': ?instance.priority,
      'tags': ?instance.tags,
      'click': ?instance.click,
    };

const _$NtfyMessageEventEnumEnumMap = {NtfyMessageEventEnum.message: 'message'};
