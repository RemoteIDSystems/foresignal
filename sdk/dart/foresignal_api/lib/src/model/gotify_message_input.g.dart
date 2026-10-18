// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gotify_message_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GotifyMessageInputCWProxy {
  GotifyMessageInput title(String? title);

  GotifyMessageInput message(String message);

  GotifyMessageInput priority(int? priority);

  GotifyMessageInput extras(Map<String, Object>? extras);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GotifyMessageInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GotifyMessageInput(...).copyWith(id: 12, name: "My name")
  /// ```
  GotifyMessageInput call({
    String? title,
    String message,
    int? priority,
    Map<String, Object>? extras,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGotifyMessageInput.copyWith(...)` or call `instanceOfGotifyMessageInput.copyWith.fieldName(value)` for a single field.
class _$GotifyMessageInputCWProxyImpl implements _$GotifyMessageInputCWProxy {
  const _$GotifyMessageInputCWProxyImpl(this._value);

  final GotifyMessageInput _value;

  @override
  GotifyMessageInput title(String? title) => call(title: title);

  @override
  GotifyMessageInput message(String message) => call(message: message);

  @override
  GotifyMessageInput priority(int? priority) => call(priority: priority);

  @override
  GotifyMessageInput extras(Map<String, Object>? extras) =>
      call(extras: extras);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GotifyMessageInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GotifyMessageInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GotifyMessageInput call({
    Object? title = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? priority = const $CopyWithPlaceholder(),
    Object? extras = const $CopyWithPlaceholder(),
  }) {
    return GotifyMessageInput(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      priority: priority == const $CopyWithPlaceholder()
          ? _value.priority
          // ignore: cast_nullable_to_non_nullable
          : priority as int?,
      extras: extras == const $CopyWithPlaceholder()
          ? _value.extras
          // ignore: cast_nullable_to_non_nullable
          : extras as Map<String, Object>?,
    );
  }
}

extension $GotifyMessageInputCopyWith on GotifyMessageInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGotifyMessageInput.copyWith(...)` or `instanceOfGotifyMessageInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GotifyMessageInputCWProxy get copyWith =>
      _$GotifyMessageInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GotifyMessageInput _$GotifyMessageInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GotifyMessageInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['message']);
      final val = GotifyMessageInput(
        title: $checkedConvert('title', (v) => v as String?),
        message: $checkedConvert('message', (v) => v as String),
        priority: $checkedConvert('priority', (v) => (v as num?)?.toInt()),
        extras: $checkedConvert(
          'extras',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GotifyMessageInputToJson(GotifyMessageInput instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'message': instance.message,
      'priority': ?instance.priority,
      'extras': ?instance.extras,
    };
