//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue_external_results_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCueExternalResultsInner {
  /// Returns a new [SocialCueExternalResultsInner] instance.
  SocialCueExternalResultsInner({

    required  this.sourceId,

    required  this.queries,

    required  this.items,

     this.error,
  });

      /// The provider's source
  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



      /// Queries sent (at most 10)
          // minimum: 0
  @JsonKey(
    
    name: r'queries',
    required: true,
    includeIfNull: false,
  )


  final int queries;



      /// Records stored as cue results
          // minimum: 0
  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final int items;



      /// The first failure, e.g. BUDGET_EXCEEDED
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCueExternalResultsInner &&
      other.sourceId == sourceId &&
      other.queries == queries &&
      other.items == items &&
      other.error == error;

    @override
    int get hashCode =>
        sourceId.hashCode +
        queries.hashCode +
        items.hashCode +
        (error == null ? 0 : error.hashCode);

  factory SocialCueExternalResultsInner.fromJson(Map<String, dynamic> json) => _$SocialCueExternalResultsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueExternalResultsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

