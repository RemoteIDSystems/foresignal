//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_sections_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentSectionsInner {
  /// Returns a new [AssessmentSectionsInner] instance.
  AssessmentSectionsInner({

    required  this.key,

    required  this.title,

    required  this.markdown,

    required  this.textOrigin,

     this.data,
  });

  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final AssessmentSectionsInnerKeyEnum key;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'markdown',
    required: true,
    includeIfNull: false,
  )


  final String markdown;



  @JsonKey(
    
    name: r'textOrigin',
    required: true,
    includeIfNull: false,
  )


  final AssessmentSectionsInnerTextOriginEnum textOrigin;



  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentSectionsInner &&
      other.key == key &&
      other.title == title &&
      other.markdown == markdown &&
      other.textOrigin == textOrigin &&
      other.data == data;

    @override
    int get hashCode =>
        key.hashCode +
        title.hashCode +
        markdown.hashCode +
        textOrigin.hashCode +
        (data == null ? 0 : data.hashCode);

  factory AssessmentSectionsInner.fromJson(Map<String, dynamic> json) => _$AssessmentSectionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentSectionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AssessmentSectionsInnerKeyEnum {
@JsonValue(r'bluf')
bluf(r'bluf'),
@JsonValue(r'what-changed')
whatChanged(r'what-changed'),
@JsonValue(r'situations')
situations(r'situations'),
@JsonValue(r'hypotheses')
hypotheses(r'hypotheses'),
@JsonValue(r'indicators')
indicators(r'indicators'),
@JsonValue(r'forecasts')
forecasts(r'forecasts'),
@JsonValue(r'speculation')
speculation(r'speculation'),
@JsonValue(r'sources')
sources(r'sources'),
@JsonValue(r'collection-gaps')
collectionGaps(r'collection-gaps'),
@JsonValue(r'key-insights')
keyInsights(r'key-insights'),
@JsonValue(r'outlook')
outlook(r'outlook'),
@JsonValue(r'risks')
risks(r'risks'),
@JsonValue(r'social')
social(r'social');

const AssessmentSectionsInnerKeyEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AssessmentSectionsInnerTextOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm'),
@JsonValue(r'mixed')
mixed(r'mixed');

const AssessmentSectionsInnerTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


