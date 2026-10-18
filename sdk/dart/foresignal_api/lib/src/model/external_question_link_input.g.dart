// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_question_link_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalQuestionLinkInputCWProxy {
  ExternalQuestionLinkInput streamId(String streamId);

  ExternalQuestionLinkInput externalQuestionId(String externalQuestionId);

  ExternalQuestionLinkInput questionId(String questionId);

  ExternalQuestionLinkInput orientation(
    ExternalQuestionLinkInputOrientationEnum? orientation,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalQuestionLinkInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalQuestionLinkInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalQuestionLinkInput call({
    String streamId,
    String externalQuestionId,
    String questionId,
    ExternalQuestionLinkInputOrientationEnum? orientation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalQuestionLinkInput.copyWith(...)` or call `instanceOfExternalQuestionLinkInput.copyWith.fieldName(value)` for a single field.
class _$ExternalQuestionLinkInputCWProxyImpl
    implements _$ExternalQuestionLinkInputCWProxy {
  const _$ExternalQuestionLinkInputCWProxyImpl(this._value);

  final ExternalQuestionLinkInput _value;

  @override
  ExternalQuestionLinkInput streamId(String streamId) =>
      call(streamId: streamId);

  @override
  ExternalQuestionLinkInput externalQuestionId(String externalQuestionId) =>
      call(externalQuestionId: externalQuestionId);

  @override
  ExternalQuestionLinkInput questionId(String questionId) =>
      call(questionId: questionId);

  @override
  ExternalQuestionLinkInput orientation(
    ExternalQuestionLinkInputOrientationEnum? orientation,
  ) => call(orientation: orientation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalQuestionLinkInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalQuestionLinkInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalQuestionLinkInput call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? externalQuestionId = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? orientation = const $CopyWithPlaceholder(),
  }) {
    return ExternalQuestionLinkInput(
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      externalQuestionId:
          externalQuestionId == const $CopyWithPlaceholder() ||
              externalQuestionId == null
          ? _value.externalQuestionId
          // ignore: cast_nullable_to_non_nullable
          : externalQuestionId as String,
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      orientation: orientation == const $CopyWithPlaceholder()
          ? _value.orientation
          // ignore: cast_nullable_to_non_nullable
          : orientation as ExternalQuestionLinkInputOrientationEnum?,
    );
  }
}

extension $ExternalQuestionLinkInputCopyWith on ExternalQuestionLinkInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalQuestionLinkInput.copyWith(...)` or `instanceOfExternalQuestionLinkInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalQuestionLinkInputCWProxy get copyWith =>
      _$ExternalQuestionLinkInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalQuestionLinkInput _$ExternalQuestionLinkInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalQuestionLinkInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['streamId', 'externalQuestionId', 'questionId'],
  );
  final val = ExternalQuestionLinkInput(
    streamId: $checkedConvert('streamId', (v) => v as String),
    externalQuestionId: $checkedConvert(
      'externalQuestionId',
      (v) => v as String,
    ),
    questionId: $checkedConvert('questionId', (v) => v as String),
    orientation: $checkedConvert(
      'orientation',
      (v) => $enumDecodeNullable(
        _$ExternalQuestionLinkInputOrientationEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalQuestionLinkInputToJson(
  ExternalQuestionLinkInput instance,
) => <String, dynamic>{
  'streamId': instance.streamId,
  'externalQuestionId': instance.externalQuestionId,
  'questionId': instance.questionId,
  'orientation':
      ?_$ExternalQuestionLinkInputOrientationEnumEnumMap[instance.orientation],
};

const _$ExternalQuestionLinkInputOrientationEnumEnumMap = {
  ExternalQuestionLinkInputOrientationEnum.same: 'same',
  ExternalQuestionLinkInputOrientationEnum.inverse: 'inverse',
};
