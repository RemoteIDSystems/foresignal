// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_note.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystNoteCWProxy {
  AnalystNote id(String id);

  AnalystNote profileId(String profileId);

  AnalystNote runId(String runId);

  AnalystNote title(String title);

  AnalystNote body(String body);

  AnalystNote priority(AnalystNotePriorityEnum priority);

  AnalystNote claims(List<AnalystClaim> claims);

  AnalystNote citations(List<String>? citations);

  AnalystNote private(bool? private);

  AnalystNote status(AnalystNoteStatusEnum status);

  AnalystNote model(String? model);

  AnalystNote footer(String? footer);

  AnalystNote createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNote(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNote(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystNote call({
    String id,
    String profileId,
    String runId,
    String title,
    String body,
    AnalystNotePriorityEnum priority,
    List<AnalystClaim> claims,
    List<String>? citations,
    bool? private,
    AnalystNoteStatusEnum status,
    String? model,
    String? footer,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystNote.copyWith(...)` or call `instanceOfAnalystNote.copyWith.fieldName(value)` for a single field.
class _$AnalystNoteCWProxyImpl implements _$AnalystNoteCWProxy {
  const _$AnalystNoteCWProxyImpl(this._value);

  final AnalystNote _value;

  @override
  AnalystNote id(String id) => call(id: id);

  @override
  AnalystNote profileId(String profileId) => call(profileId: profileId);

  @override
  AnalystNote runId(String runId) => call(runId: runId);

  @override
  AnalystNote title(String title) => call(title: title);

  @override
  AnalystNote body(String body) => call(body: body);

  @override
  AnalystNote priority(AnalystNotePriorityEnum priority) =>
      call(priority: priority);

  @override
  AnalystNote claims(List<AnalystClaim> claims) => call(claims: claims);

  @override
  AnalystNote citations(List<String>? citations) => call(citations: citations);

  @override
  AnalystNote private(bool? private) => call(private: private);

  @override
  AnalystNote status(AnalystNoteStatusEnum status) => call(status: status);

  @override
  AnalystNote model(String? model) => call(model: model);

  @override
  AnalystNote footer(String? footer) => call(footer: footer);

  @override
  AnalystNote createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNote(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNote(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystNote call({
    Object? id = const $CopyWithPlaceholder(),
    Object? profileId = const $CopyWithPlaceholder(),
    Object? runId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? priority = const $CopyWithPlaceholder(),
    Object? claims = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? footer = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return AnalystNote(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      profileId: profileId == const $CopyWithPlaceholder() || profileId == null
          ? _value.profileId
          // ignore: cast_nullable_to_non_nullable
          : profileId as String,
      runId: runId == const $CopyWithPlaceholder() || runId == null
          ? _value.runId
          // ignore: cast_nullable_to_non_nullable
          : runId as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      body: body == const $CopyWithPlaceholder() || body == null
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String,
      priority: priority == const $CopyWithPlaceholder() || priority == null
          ? _value.priority
          // ignore: cast_nullable_to_non_nullable
          : priority as AnalystNotePriorityEnum,
      claims: claims == const $CopyWithPlaceholder() || claims == null
          ? _value.claims
          // ignore: cast_nullable_to_non_nullable
          : claims as List<AnalystClaim>,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AnalystNoteStatusEnum,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      footer: footer == const $CopyWithPlaceholder()
          ? _value.footer
          // ignore: cast_nullable_to_non_nullable
          : footer as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $AnalystNoteCopyWith on AnalystNote {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystNote.copyWith(...)` or `instanceOfAnalystNote.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystNoteCWProxy get copyWith => _$AnalystNoteCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystNote _$AnalystNoteFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystNote', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'profileId',
          'runId',
          'title',
          'body',
          'priority',
          'claims',
          'status',
          'createdAt',
        ],
      );
      final val = AnalystNote(
        id: $checkedConvert('id', (v) => v as String),
        profileId: $checkedConvert('profileId', (v) => v as String),
        runId: $checkedConvert('runId', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        body: $checkedConvert('body', (v) => v as String),
        priority: $checkedConvert(
          'priority',
          (v) => $enumDecode(_$AnalystNotePriorityEnumEnumMap, v),
        ),
        claims: $checkedConvert(
          'claims',
          (v) => (v as List<dynamic>)
              .map((e) => AnalystClaim.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        citations: $checkedConvert(
          'citations',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        private: $checkedConvert('private', (v) => v as bool?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$AnalystNoteStatusEnumEnumMap, v),
        ),
        model: $checkedConvert('model', (v) => v as String?),
        footer: $checkedConvert('footer', (v) => v as String?),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalystNoteToJson(AnalystNote instance) =>
    <String, dynamic>{
      'id': instance.id,
      'profileId': instance.profileId,
      'runId': instance.runId,
      'title': instance.title,
      'body': instance.body,
      'priority': _$AnalystNotePriorityEnumEnumMap[instance.priority]!,
      'claims': instance.claims.map((e) => e.toJson()).toList(),
      'citations': ?instance.citations,
      'private': ?instance.private,
      'status': _$AnalystNoteStatusEnumEnumMap[instance.status]!,
      'model': ?instance.model,
      'footer': ?instance.footer,
      'createdAt': instance.createdAt.toIso8601String(),
    };

const _$AnalystNotePriorityEnumEnumMap = {
  AnalystNotePriorityEnum.low: 'low',
  AnalystNotePriorityEnum.normal: 'normal',
  AnalystNotePriorityEnum.high: 'high',
};

const _$AnalystNoteStatusEnumEnumMap = {
  AnalystNoteStatusEnum.new_: 'new',
  AnalystNoteStatusEnum.read: 'read',
  AnalystNoteStatusEnum.dismissed: 'dismissed',
};
