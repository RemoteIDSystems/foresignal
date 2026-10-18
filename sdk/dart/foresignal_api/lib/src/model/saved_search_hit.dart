//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_hit.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchHit {
  /// Returns a new [SavedSearchHit] instance.
  SavedSearchHit({

    required  this.envelope,

     this.score,

     this.matchedBy,
  });

  @JsonKey(
    
    name: r'envelope',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope envelope;



  @JsonKey(
    
    name: r'score',
    required: false,
    includeIfNull: false,
  )


  final num? score;



  @JsonKey(
    
    name: r'matchedBy',
    required: false,
    includeIfNull: false,
  )


  final SavedSearchHitMatchedByEnum? matchedBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchHit &&
      other.envelope == envelope &&
      other.score == score &&
      other.matchedBy == matchedBy;

    @override
    int get hashCode =>
        envelope.hashCode +
        (score == null ? 0 : score.hashCode) +
        matchedBy.hashCode;

  factory SavedSearchHit.fromJson(Map<String, dynamic> json) => _$SavedSearchHitFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchHitToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SavedSearchHitMatchedByEnum {
@JsonValue(r'text')
text(r'text'),
@JsonValue(r'semantic')
semantic(r'semantic'),
@JsonValue(r'hybrid')
hybrid(r'hybrid'),
@JsonValue(r'filter')
filter(r'filter');

const SavedSearchHitMatchedByEnum(this.value);

final String value;

@override
String toString() => value;
}


