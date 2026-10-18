// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ntfy_publish_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NtfyPublishRequestCWProxy {
  NtfyPublishRequest topic(String topic);

  NtfyPublishRequest message(String? message);

  NtfyPublishRequest title(String? title);

  NtfyPublishRequest priority(int? priority);

  NtfyPublishRequest tags(List<String>? tags);

  NtfyPublishRequest click(String? click);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NtfyPublishRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NtfyPublishRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  NtfyPublishRequest call({
    String topic,
    String? message,
    String? title,
    int? priority,
    List<String>? tags,
    String? click,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNtfyPublishRequest.copyWith(...)` or call `instanceOfNtfyPublishRequest.copyWith.fieldName(value)` for a single field.
class _$NtfyPublishRequestCWProxyImpl implements _$NtfyPublishRequestCWProxy {
  const _$NtfyPublishRequestCWProxyImpl(this._value);

  final NtfyPublishRequest _value;

  @override
  NtfyPublishRequest topic(String topic) => call(topic: topic);

  @override
  NtfyPublishRequest message(String? message) => call(message: message);

  @override
  NtfyPublishRequest title(String? title) => call(title: title);

  @override
  NtfyPublishRequest priority(int? priority) => call(priority: priority);

  @override
  NtfyPublishRequest tags(List<String>? tags) => call(tags: tags);

  @override
  NtfyPublishRequest click(String? click) => call(click: click);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NtfyPublishRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NtfyPublishRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NtfyPublishRequest call({
    Object? topic = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? priority = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? click = const $CopyWithPlaceholder(),
  }) {
    return NtfyPublishRequest(
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

extension $NtfyPublishRequestCopyWith on NtfyPublishRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNtfyPublishRequest.copyWith(...)` or `instanceOfNtfyPublishRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NtfyPublishRequestCWProxy get copyWith =>
      _$NtfyPublishRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NtfyPublishRequest _$NtfyPublishRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NtfyPublishRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['topic']);
      final val = NtfyPublishRequest(
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

Map<String, dynamic> _$NtfyPublishRequestToJson(NtfyPublishRequest instance) =>
    <String, dynamic>{
      'topic': instance.topic,
      'message': ?instance.message,
      'title': ?instance.title,
      'priority': ?instance.priority,
      'tags': ?instance.tags,
      'click': ?instance.click,
    };
