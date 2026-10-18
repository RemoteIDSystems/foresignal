// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskRequestCWProxy {
  AskRequest question(String question);

  AskRequest threadId(String? threadId);

  AskRequest scope(AskScope? scope);

  AskRequest allowActions(bool? allowActions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  AskRequest call({
    String question,
    String? threadId,
    AskScope? scope,
    bool? allowActions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskRequest.copyWith(...)` or call `instanceOfAskRequest.copyWith.fieldName(value)` for a single field.
class _$AskRequestCWProxyImpl implements _$AskRequestCWProxy {
  const _$AskRequestCWProxyImpl(this._value);

  final AskRequest _value;

  @override
  AskRequest question(String question) => call(question: question);

  @override
  AskRequest threadId(String? threadId) => call(threadId: threadId);

  @override
  AskRequest scope(AskScope? scope) => call(scope: scope);

  @override
  AskRequest allowActions(bool? allowActions) =>
      call(allowActions: allowActions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskRequest call({
    Object? question = const $CopyWithPlaceholder(),
    Object? threadId = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? allowActions = const $CopyWithPlaceholder(),
  }) {
    return AskRequest(
      question: question == const $CopyWithPlaceholder() || question == null
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as String,
      threadId: threadId == const $CopyWithPlaceholder()
          ? _value.threadId
          // ignore: cast_nullable_to_non_nullable
          : threadId as String?,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as AskScope?,
      allowActions: allowActions == const $CopyWithPlaceholder()
          ? _value.allowActions
          // ignore: cast_nullable_to_non_nullable
          : allowActions as bool?,
    );
  }
}

extension $AskRequestCopyWith on AskRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskRequest.copyWith(...)` or `instanceOfAskRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskRequestCWProxy get copyWith => _$AskRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskRequest _$AskRequestFromJson(Map<String, dynamic> json) => $checkedCreate(
  'AskRequest',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['question']);
    final val = AskRequest(
      question: $checkedConvert('question', (v) => v as String),
      threadId: $checkedConvert('threadId', (v) => v as String?),
      scope: $checkedConvert(
        'scope',
        (v) => v == null ? null : AskScope.fromJson(v as Map<String, dynamic>),
      ),
      allowActions: $checkedConvert('allowActions', (v) => v as bool? ?? false),
    );
    return val;
  },
);

Map<String, dynamic> _$AskRequestToJson(AskRequest instance) =>
    <String, dynamic>{
      'question': instance.question,
      'threadId': ?instance.threadId,
      'scope': ?instance.scope?.toJson(),
      'allowActions': ?instance.allowActions,
    };
