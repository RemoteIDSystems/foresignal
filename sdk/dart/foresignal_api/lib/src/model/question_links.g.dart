// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'question_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuestionLinksCWProxy {
  QuestionLinks situationId(String? situationId);

  QuestionLinks hypothesisSetId(String? hypothesisSetId);

  QuestionLinks precursorId(String? precursorId);

  QuestionLinks findingId(String? findingId);

  QuestionLinks seriesRef(String? seriesRef);

  QuestionLinks outlookId(String? outlookId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuestionLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuestionLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  QuestionLinks call({
    String? situationId,
    String? hypothesisSetId,
    String? precursorId,
    String? findingId,
    String? seriesRef,
    String? outlookId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuestionLinks.copyWith(...)` or call `instanceOfQuestionLinks.copyWith.fieldName(value)` for a single field.
class _$QuestionLinksCWProxyImpl implements _$QuestionLinksCWProxy {
  const _$QuestionLinksCWProxyImpl(this._value);

  final QuestionLinks _value;

  @override
  QuestionLinks situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  QuestionLinks hypothesisSetId(String? hypothesisSetId) =>
      call(hypothesisSetId: hypothesisSetId);

  @override
  QuestionLinks precursorId(String? precursorId) =>
      call(precursorId: precursorId);

  @override
  QuestionLinks findingId(String? findingId) => call(findingId: findingId);

  @override
  QuestionLinks seriesRef(String? seriesRef) => call(seriesRef: seriesRef);

  @override
  QuestionLinks outlookId(String? outlookId) => call(outlookId: outlookId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuestionLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuestionLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  QuestionLinks call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? hypothesisSetId = const $CopyWithPlaceholder(),
    Object? precursorId = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? seriesRef = const $CopyWithPlaceholder(),
    Object? outlookId = const $CopyWithPlaceholder(),
  }) {
    return QuestionLinks(
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      hypothesisSetId: hypothesisSetId == const $CopyWithPlaceholder()
          ? _value.hypothesisSetId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetId as String?,
      precursorId: precursorId == const $CopyWithPlaceholder()
          ? _value.precursorId
          // ignore: cast_nullable_to_non_nullable
          : precursorId as String?,
      findingId: findingId == const $CopyWithPlaceholder()
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String?,
      seriesRef: seriesRef == const $CopyWithPlaceholder()
          ? _value.seriesRef
          // ignore: cast_nullable_to_non_nullable
          : seriesRef as String?,
      outlookId: outlookId == const $CopyWithPlaceholder()
          ? _value.outlookId
          // ignore: cast_nullable_to_non_nullable
          : outlookId as String?,
    );
  }
}

extension $QuestionLinksCopyWith on QuestionLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuestionLinks.copyWith(...)` or `instanceOfQuestionLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuestionLinksCWProxy get copyWith => _$QuestionLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QuestionLinks _$QuestionLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('QuestionLinks', json, ($checkedConvert) {
      final val = QuestionLinks(
        situationId: $checkedConvert('situationId', (v) => v as String?),
        hypothesisSetId: $checkedConvert(
          'hypothesisSetId',
          (v) => v as String?,
        ),
        precursorId: $checkedConvert('precursorId', (v) => v as String?),
        findingId: $checkedConvert('findingId', (v) => v as String?),
        seriesRef: $checkedConvert('seriesRef', (v) => v as String?),
        outlookId: $checkedConvert('outlookId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$QuestionLinksToJson(QuestionLinks instance) =>
    <String, dynamic>{
      'situationId': ?instance.situationId,
      'hypothesisSetId': ?instance.hypothesisSetId,
      'precursorId': ?instance.precursorId,
      'findingId': ?instance.findingId,
      'seriesRef': ?instance.seriesRef,
      'outlookId': ?instance.outlookId,
    };
